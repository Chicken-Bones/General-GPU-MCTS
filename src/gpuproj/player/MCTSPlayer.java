package gpuproj.player;

import gpuproj.StatDialog;
import gpuproj.simulator.PlayoutSimulator;
import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;

import java.util.Arrays;

public class MCTSPlayer<B extends Board<B>> extends Player<B>
{
    public TreeNode<B> tree;
    public PlayoutSimulator simulator;

    public MCTSPlayer(BoardGame<B> game, PlayoutSimulator simulator) {
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
        do {
            //selection
            TreeNode<B> node = tree;
            while(!node.isLeaf())
                node = node.select();

            node.expand(game);
            if(node.isLeaf())//terminating node
                simulator.play(Arrays.asList(node), game);//get a better estimate
            else
                simulator.play(node.children, game);
        } while(System.currentTimeMillis() - start < limit);

        StatDialog.get("Tree Depth").setText(""+tree.getDepth());

        int maxCount = 0;
        for(TreeNode<B> c : tree.children)
            if(c.sims > maxCount) {
                tree = c;
                maxCount = c.sims;
            }

        Move<B> move = tree.move;
        tree.makeRoot();//makes tree.move into a complete board
        return move;
    }

    @Override
    public void applyMove(Move<B> move) {
        tree.expand(game);//ensure expanded
        for(TreeNode<B> c : tree.children)
            if(c.move.equals(move)) {
                tree = c;
                tree.makeRoot();
                return;
            }

        throw new IllegalStateException("Could not find corresponding TreeNode for move:\n"+move);
    }

    @Override
    public String toString() {
        return "MCTS ("+simulator+")";
    }
}
