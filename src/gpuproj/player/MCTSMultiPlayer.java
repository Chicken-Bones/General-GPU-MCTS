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
    private class CPUConsumer implements Runnable
    {
        @Override
        public void run() {
            while(simulating) {
                TreeNode<B> node;
                try {
                    node = simQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }

                node.update(BoardGame.floatScore(PlayoutSimulator.playout(node.getBoardCopy(), game), 1), 1);
                PlayoutSimulator.simCount++;

                synchronized (doneQueue) {
                    doneQueue.add((MultiTreeNode<B>) node);
                }
            }
        }
    }

    private static final int GPUBatchSize = 128;
    private static final int GPUSimsPerNode = 8;
    private class GPUConsumer implements Runnable
    {
        private BoardGameKernel kernel;
        private ArrayList<MultiTreeNode<B>> nodes = new ArrayList<MultiTreeNode<B>>(128);

        public GPUConsumer(Class<? extends BoardGame> gameClass) {
            kernel = new BoardGameKernel(gameClass, GPUBatchSize);
        }

        @Override
        public void run() {
            while(simulating) {
                while(nodes.size() < GPUBatchSize && simulating)
                    try {
                        nodes.add(simQueue.take());
                    } catch (InterruptedException ignored) {}

                if(simulating) {
                    kernel.run((List) nodes, GPUSimsPerNode);
                    PlayoutSimulator.simCount += nodes.size() * GPUSimsPerNode;
                }

                synchronized (doneQueue) {
                    doneQueue.addAll(nodes);
                }

                nodes.clear();
            }
        }
    }

    private String name;
    private LinkedList<Runnable> consumers = new LinkedList<Runnable>();

    private final BlockingQueue<MultiTreeNode<B>> simQueue = new ArrayBlockingQueue<MultiTreeNode<B>>(256);
    private final Queue<MultiTreeNode<B>> doneQueue = new LinkedList<MultiTreeNode<B>>();
    private volatile boolean simulating;

    public MCTSMultiPlayer(BoardGame<B> game, int threads, boolean gpu) {
        super(game, null);
        name = "Hybrid("+threads+", "+gpu+")";
        for(int i = 0; i < threads; i++)
            consumers.add(new CPUConsumer());

        if(gpu)
            consumers.add(new GPUConsumer(game.getClass()));
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
            throw new IllegalStateException("What?");
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
        simulating = true;
        LinkedList<Thread> threads = new LinkedList<Thread>();
        for(Runnable c : consumers) {
            Thread t = new Thread(c);
            t.start();
            threads.add(t);
        }

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
                throw new RuntimeException("What?");
            }
        }

        simulating = false;
        for(Thread t : threads) {
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("What?");
            }
        }

        flushDone();
    }

    @Override
    public String toString() {
        return name;
    }
}
