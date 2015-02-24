package gpuproj.player;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;

public class MultiTreeNode<B extends Board<B>> extends TreeNode<B>
{
    /**
     * For a leaf node, 1 if this node is queued for simulation
     * Otherwise, contains the number of children completely queued for simulation
     */
    public int queued;

    public MultiTreeNode(Move<B> move, BoardGame<B> game) {
        super(move, game);
    }

    public MultiTreeNode(Move<B> move, TreeNode<B> parent) {
        super(move, parent);
    }

    public void queue() {
        queued++;

        if(isQueued() && !isRoot())
            ((MultiTreeNode)parent).queue();
    }

    public void dequeue() {
        if(isQueued() && !isRoot())
            ((MultiTreeNode)parent).dequeue();

        queued--;
    }

    public boolean isQueued() {
        return queued == (isLeaf() ? 1 : children.size());
    }

    @Override
    public double getUCBScore() {
        return isQueued() ? Double.NEGATIVE_INFINITY : super.getUCBScore();
    }

    @Override
    public TreeNode<B> newChild(Move<B> move) {
        return new MultiTreeNode<B>(move, this);
    }
}
