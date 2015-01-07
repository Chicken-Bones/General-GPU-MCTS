package gpuproj.player;

import gpuproj.simulator.PlayoutSimulator;
import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;

import java.util.Arrays;

public class UCBPlayer<B extends Board<B>> extends Player<B>
{
    public TreeNode<B> tree;
    public PlayoutSimulator simulator;

    public UCBPlayer(BoardGame<B> game, PlayoutSimulator simulator) {
        super(game);
        this.simulator = simulator;
    }

    @Override
    public void startGame(B board) {
        tree = new TreeNode<B>(board, game);
    }

    @Override
    public Move<B> selectMove(long limit) {
        long start = System.currentTimeMillis();
        tree.expand(game);
        simulator.play(tree.children, game);
        while(System.currentTimeMillis() - start < limit) {
            //selection
            TreeNode<B> node = tree.select();
            simulator.play(Arrays.asList(node), game);//get a better estimate
        }

        int maxCount = 0;
        for(TreeNode<B> c : tree.children)
            if(c.sims > maxCount) {
                tree = c;
                maxCount = c.sims;
            }

        return tree.move;
    }

    @Override
    public void applyMove(Move<B> move) {
        tree = new TreeNode<B>(move.apply(tree.getBoardCopy()), game);
    }

    @Override
    public String toString() {
        return "UCB ("+simulator+")";
    }
}
