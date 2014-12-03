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

    private <B extends Board<B>> void play(TreeNode<B> node, BoardGame<B> game) {
        B board = node.getBoardCopy();
        int player = board.getTurn()^1;
        int winner = game.checkWinner(board);
        while(winner < 0) {
            game.playRandomMove(board);
            winner = game.checkWinner(board);
        }
        node.update(BoardGame.score(winner, player), 1);
    }

    @Override
    public <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game) {
        for(TreeNode<B> node : nodes)
            play(node, game);

        simCount += nodes.size();
    }
}