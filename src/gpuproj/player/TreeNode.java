package gpuproj.player;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;

import java.util.LinkedList;
import java.util.Random;

/**
 * A node in an MCTS tree, any node can be root.
 * @param <B> The board class
 */
public class TreeNode<B extends Board<B>>
{
    private static final Random tiebreaker = new Random();

    /**
     * This field stores either a board, or a move.
     * If this node is the root, it is guaranteed to store a board
     */
    public Move<B> move;
    public final BoardGame<B> game;
    public TreeNode<B> parent;
    public LinkedList<TreeNode<B>> children;
    public float wins;
    public int sims;

    public TreeNode(Move<B> move, BoardGame<B> game) {
        this.move = move;
        this.game = game;
    }

    public TreeNode(Move<B> move, TreeNode<B> parent) {
        this(move, parent.game);
        this.parent = parent;
    }

    /**
     * @return true if this is the root node in the tree (has no parent)
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * @return true if this node is unexpanded, or terminating (children is null or empty)
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * @return A copy of the board state for this node, safe to mutate. May be built by recursive calls to parent
     */
    public B getBoardCopy() {
        return move instanceof Board ? ((B)move).copy() : move.apply(parent.getBoardCopy());
    }

    /**
     * @return A the board state for this node, may not be safe to mutate. May be built by recursive calls to parent
     */
    public B getBoard() {
        return move instanceof Board ? (B)move : move.apply(parent.getBoardCopy());
    }

    /**
     * Creates child nodes for all possible moves of game from this board state.
     */
    public void expand(BoardGame<B> game) {
        if(children != null) return;

        B board = getBoard();
        children = new LinkedList<TreeNode<B>>();
        if(game.checkWinner(board) < 0)//check end condition, report no children
            for(Move<B> b : game.expand(board))
                children.add(newChild(b));
    }

    protected TreeNode<B> newChild(Move<B> move) {
        return new TreeNode<B>(move, this);
    }

    /**
     * @return The UCB score for this node relative to its parent
     */
    public double getUCBScore() {
        if(isRoot()) return Double.NEGATIVE_INFINITY;
        return wins/sims + game.getExploration()*Math.sqrt(Math.log(parent.sims)/sims);
    }

    /**
     * Selects the best node for expanding tree based on UCT
     * Assumes each child has at least 1 simulation
     * Throws an IllegalStateException if there are no child nodes
     */
    public TreeNode<B> select() {
        if(isLeaf())
            return null;

        double maxScore = Double.NEGATIVE_INFINITY;
        TreeNode<B> selected = null;
        for(TreeNode<B> c : children) {
            double score = c.getUCBScore() + tiebreaker.nextDouble()*1E-5;
            if(score > maxScore) {
                selected = c;
                maxScore = score;
            }
        }
        if(selected == null)
            throw new IllegalStateException("No child found: "+this);

        return selected;
    }

    /**
     * Propagate the results of a simulation back through the tree
     * @param score The number of wins, must be on range [0-games]
     * @param games The number of played games
     */
    public void update(float score, int games) {
        wins += score;
        sims += games;
        if(!isRoot()) parent.update(games-score, games);
    }

    /**
     * Makes this node the root, discarding the parent reference, and setting move to a complete board state
     */
    public void makeRoot() {
        move = getBoard();
        parent = null;
    }

    /**
     * @return The total number of child nodes as a tree from this root (including this node)
     */
    public int getSize() {
        int s = 1;
        if(!isLeaf()) {
            for (TreeNode<B> c : children)
                s += c.getSize();
        }

        return s;
    }

    public int getDepth() {
        int d = 0;
        if(!isLeaf()) {
            for (TreeNode<B> c : children) {
                int d2 = c.getDepth();
                if (d2 > d) d = d2;
            }
        }

        return d+1;
    }

    @Override
    public String toString() {
        return "TreeNode. "+wins+"/"+sims+" UCB: "+getUCBScore()+".\n"+(isLeaf() ? "Leaf" : "Children: "+children.size()+" Size: "+getSize()+" Depth: "+getDepth())+"\n"+getBoard();
    }
}
