package gpuproj.simulator;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.TreeNode;

import java.util.List;

/**
 * Plays one game for each node, single threaded
 */
public class CPU1Simulator extends PlayoutSimulator
{
    public static final CPU1Simulator instance = new CPU1Simulator();

    private CPU1Simulator() {}

    @Override
    public <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game) {
        for(TreeNode<B> node : nodes)
            node.update(BoardGame.floatScore(playout(node.getBoardCopy(), game), 1), 1);

        simCount += nodes.size();
    }
}