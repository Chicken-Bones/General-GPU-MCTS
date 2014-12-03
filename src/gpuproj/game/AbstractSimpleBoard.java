package gpuproj.game;

import static gpuproj.game.BitBoard.SQUARE;
import static gpuproj.game.BitBoard.format8;
import static gpuproj.game.BitBoard.overlay;

/**
 * 8x8 turn alternating bitboard with white and black pieces
 * @param <T> {@inheritDoc}
 */
public abstract class AbstractSimpleBoard<T extends AbstractSimpleBoard> extends Board<T>
{
    public long white;
    public long black;
    public byte turn;

    @Override
    public int getTurn() {
        return turn;
    }

    public long get(int turn) {
        return turn == 0 ? white : black;
    }

    public void set(int turn, long board) {
        if(turn == 0) white = board;
        else black = board;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AbstractSimpleBoard)) return false;
        AbstractSimpleBoard b = (AbstractSimpleBoard) obj;
        return white == b.white && black == b.black && turn == b.turn;
    }

    @Override
    public T set(T b) {
        white = b.white;
        black = b.black;
        turn = b.turn;
        return (T)this;
    }

    @Override
    public String toString() {
        return "Turn: "+(turn == 0 ? "White" : "Black") + '\n'+format();
    }

    protected String format() {
        return overlay(format8(0, SQUARE), format8(white, 'w'), format8(black, 'b'));
    }
}
