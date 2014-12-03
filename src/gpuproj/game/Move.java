package gpuproj.game;

/**
 * Interface for class representing a move, can be either a delta to a board, or an entire board
 * Must override equals
 */
public interface Move<B extends Board>
{
    /**
     * Apply this move to board.
     * @return board (for chaining).
     */
    public B apply(B board);
}
