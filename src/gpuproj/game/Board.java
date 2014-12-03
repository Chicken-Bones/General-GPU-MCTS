package gpuproj.game;

import gpuproj.StructLike;

/**
 * Parent class for game boards.
 * (Player1, Player2) (0, 1) (white, black) are equivalent
 * Implements move, as all boards can be used as moves instead of a delta
 * @param <T> the subclass type
 */
public abstract class Board<T extends Board> implements StructLike<T>, Move<T>
{
    /**
     * Return the player whose turn it is to play on the given board (0, 1)
     */
    public abstract int getTurn();

    @Override
    public T apply(T board) {
        return (T) board.set(this);
    }
}
