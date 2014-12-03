package gpuproj.games;

import gpuproj.game.BitBoard;
import gpuproj.game.Board;

public class AtaxxBoard2 extends Board<AtaxxBoard2>
{
    /**
     * Array of board pieces. 0 for empty, 1 for p1, 2 for p2, -1 for blocked
     */
    byte[][] board = new byte[8][8];
    byte turn;

    @Override
    public int getTurn() {
        return turn;
    }

    @Override
    public AtaxxBoard2 set(AtaxxBoard2 b) {
        for(int x = 0; x < 8; x++)
            System.arraycopy(b.board[x], 0, board[x], 0, 8);
        turn = b.turn;
        return this;
    }

    @Override
    public AtaxxBoard2 copy() {
        return new AtaxxBoard2().set(this);
    }

    @Override
    public String toString() {
        return "Turn: "+(turn == 0 ? "White" : "Black") + '\n'+
                BitBoard.format(board, 1, 'w', 2, 'b', -1, 'x');
    }
}
