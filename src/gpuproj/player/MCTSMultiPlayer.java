package gpuproj.player;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.simulator.GPUSimulator.BoardGameKernel;
import gpuproj.simulator.PlayoutSimulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MCTSMultiPlayer<B extends Board<B>> extends MCTSPlayer<B>
{
    private interface Consumer
    {
        void take() throws InterruptedException;
        boolean full();
        void sim();
        void queueDone();
    }

    private class ConsumerThread extends Thread
    {
        private Consumer consumer;
        private boolean join = false;
        private boolean sleep = true;
        private final Object sleepingLock = new Object();

        public ConsumerThread(Consumer consumer) {
            super(consumer.toString());
            this.consumer = consumer;
            start();
        }

        public void wake() {
            sleep = false;
            synchronized (sleepingLock) {
                sleepingLock.notify();//wake this sleeping thread
            }
        }

        public void sleep() {
            sleep = true;
            interrupt();
            synchronized (sleepingLock) {
                try {
                    sleepingLock.wait();//wait for a notifaction of successful sleep
                } catch (InterruptedException e) {
                    throw new IllegalStateException("What?", e);
                }
            }
        }

        public void release() {
            join = true;
            wake();
            try {
                join();
            } catch (InterruptedException e) {
                throw new IllegalStateException("What?", e);
            }
        }

        private void doSleep() {
            synchronized (sleepingLock) {
                sleepingLock.notify(); //notify the main thread know that we are now sleeping
            }

            synchronized (sleepingLock) {
                try {
                    sleepingLock.wait();
                } catch (InterruptedException e) {}
            }
        }

        private void doSim() {
            while(!consumer.full()) {
                try {
                    consumer.take();
                } catch (InterruptedException e) {
                    if(!sleep)
                        throw new IllegalStateException("Unknown interrupt reason");

                    consumer.queueDone();
                    return;
                }
            }

            consumer.sim();
            consumer.queueDone();
        }

        @Override
        public void run() {
            while(!join) {
                if(sleep)
                    doSleep();
                else
                    doSim();
            }
        }
    }

    private class CPUConsumer implements Consumer
    {
        TreeNode<B> node;

        @Override
        public void take() throws InterruptedException {
            node = simQueue.take();
        }

        @Override
        public boolean full() {
            return node != null;
        }

        @Override
        public void sim() {
            node.update(BoardGame.floatScore(PlayoutSimulator.playout(node.getBoardCopy(), game), 1), 1);
            PlayoutSimulator.simCount++;
        }

        @Override
        public void queueDone() {
            if(node != null) {
                synchronized (doneQueue) {
                    doneQueue.add((MultiTreeNode<B>) node);
                }
            }
            node = null;
        }
    }

    private static final int GPUBatchSize = 128;
    private static final int GPUSimsPerNode = 8;
    private class GPUConsumer implements Consumer
    {
        private BoardGameKernel kernel;
        private ArrayList<MultiTreeNode<B>> nodes = new ArrayList<MultiTreeNode<B>>(128);

        public GPUConsumer(Class<? extends BoardGame> gameClass) {
            kernel = new BoardGameKernel(gameClass, GPUBatchSize);
        }

        @Override
        public void take() throws InterruptedException {
            nodes.add(simQueue.take());
        }

        @Override
        public boolean full() {
            return nodes.size() == GPUBatchSize;
        }

        @Override
        public void sim() {
            kernel.run((List) nodes, GPUSimsPerNode);
            PlayoutSimulator.simCount += nodes.size() * GPUSimsPerNode;
        }

        @Override
        public void queueDone() {
            synchronized (doneQueue) {
                doneQueue.addAll(nodes);
            }
            nodes.clear();
        }
    }

    private String name;
    private LinkedList<ConsumerThread> consumers = new LinkedList<ConsumerThread>();

    private final BlockingQueue<MultiTreeNode<B>> simQueue = new ArrayBlockingQueue<MultiTreeNode<B>>(256);
    private final Queue<MultiTreeNode<B>> doneQueue = new LinkedList<MultiTreeNode<B>>();

    public MCTSMultiPlayer(BoardGame<B> game, int threads, boolean gpu) {
        super(game, null);
        name = "Hybrid("+threads+", "+gpu+")";
        for(int i = 0; i < threads; i++)
            consumers.add(new ConsumerThread(new CPUConsumer()));

        if(gpu)
            consumers.add(new ConsumerThread(new GPUConsumer(game.getClass())));
    }

    @Override
    public void startGame(B board) {
        tree = new MultiTreeNode<B>(board, game);
    }

    private void queue(MultiTreeNode<B> node) {
        node.queue();
        try {
            simQueue.put(node);
        } catch (InterruptedException e) {
            throw new IllegalStateException("What?", e);
        }
    }

    private void flushDone() {
        synchronized (doneQueue) {
            while(!doneQueue.isEmpty())
                doneQueue.remove().dequeue();
        }
    }

    @Override
    protected void mcts(long limit) {
        for(ConsumerThread c : consumers)
            c.wake();

        long start = System.currentTimeMillis();
        do {
            MultiTreeNode<B> node = (MultiTreeNode<B>) tree;
            if(!node.isQueued()) {
                while(!node.isLeaf())
                    node = (MultiTreeNode<B>) node.select();

                node.expand(game);
                PlayoutSimulator.expCount++;
                if(node.isLeaf())
                    queue(node);
                else
                    for(TreeNode<B> c : node.children)
                        queue((MultiTreeNode<B>) c);
            }

            flushDone();
        } while(System.currentTimeMillis() - start < limit);

        synchronized (simQueue) {
            while(!simQueue.isEmpty()) try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException("What?", e);
            }
        }

        for(ConsumerThread c : consumers)
            c.sleep();

        flushDone();
    }

    @Override
    public String toString() {
        return name;
    }
}
