package gpuproj.simulator;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.TreeNode;

import java.util.List;

/**
 * Single Threaded CPU Implementation
 */
public class CPUBlockSimulator extends PlayoutSimulator
{
    private int simsPerNode;

    public CPUBlockSimulator(int simsPerNode) {
        this.simsPerNode = simsPerNode;
    }

    @Override
    public <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game) {
        for(TreeNode<B> node : nodes) {
            int score = 0;
            for(int i = 0; i < simsPerNode; i++)
                score += playout(node.getBoardCopy(), game);

            node.update(BoardGame.floatScore(score, simsPerNode), simsPerNode);
        }

        simCount += nodes.size() * simsPerNode;
        expCount++;
    }

    @Override
    public String toString() {
        return "CPU "+simsPerNode+"/node";
    }
}