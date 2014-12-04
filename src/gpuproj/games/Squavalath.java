package gpuproj.games;

import gpuproj.Portable;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;
import gpuproj.game.SimpleBoard;

import java.util.ArrayList;
import java.util.List;

import static gpuproj.game.BitBoard.shiftDir;

/**
 * 8x8 4 or more in a row to win, 3 in a row to lose
 */
public class Squavalath extends BoardGame<SimpleBoard>
{
    @Override
    public SimpleBoard getStartingBoard() {
        return new SimpleBoard();
    }

    private ArrayList<SimpleBoard> moves = new ArrayList<SimpleBoard>();
    @Override
    public List<? extends Move<SimpleBoard>> expand(SimpleBoard board) {
        moves.clear();

        long empty = ~(board.white|board.black);
        while(empty != 0) {
            long move = empty & -empty;

            SimpleBoard next = board.copy();
            next.set(board.turn, next.get(board.turn) | move);
            next.turn ^= 1;
            moves.add(next);

            empty &= ~move;
        }

        return moves;
    }

    /**
     * @return 0, if board contains a line of 3, 1 if board contains a line of more than 4, -1 otherwise
     */
    private int checkWin(long board) {
        for(int a = 0; a < 4; a++) {//iterate axes
            long plus3 = board;//bits mark lines of 3 or more
            for(int i = 0; i < 2; i++)
                plus3 &= shiftDir(plus3, a);
            //erode and dilate to get plus3 without lines exactly 3
            long plus4 = plus3 & shiftDir(plus3, a);
            plus4 |= shiftDir(plus4, a+4);
            if((plus3 & ~plus4) != 0)//exactly 3
                return 0;
            if(plus4 != 0)
                return 1;
        }
        return -1;
    }

    @Override
    public int checkWinner(SimpleBoard board) {
        switch(checkWin(board.get(board.turn ^ 1))) {//player who just went
            case 0: return board.turn;
            case 1: return board.turn ^ 1;
        }

        //draw if board is full
        return (board.white|board.black) == -1 ? 2 : -1;
    }

    @Override
    public void playRandomMove(SimpleBoard board) {
        long empty = ~(board.white|board.black);
        int randmove = Portable.randInt(Long.bitCount(empty));
        for(int i = 0;; i++) {
            long move = empty & -empty;
            if (i == randmove) {
                board.set(board.turn, board.get(board.turn) | move);
                board.turn ^= 1;
                return;
            }

            empty &= ~move;
        }
    }
}
