package gpuproj.games;

import gpuproj.util.Portable;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;
import gpuproj.game.SimpleBoard;

import java.util.ArrayList;
import java.util.List;

import static gpuproj.game.BitBoard.*;

/**
 * 8x8 5 in a row exactly to win
 */
public class GoMoku extends BoardGame<SimpleBoard>
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
     * @return true if board contains a line of exactly 5 in any of 8 directions
     */
    private boolean checkWin(long board) {
        for(int a = 0; a < 4; a++) {//iterate axes
            long plus5 = board;//bits mark lines of 5 or more
            for(int i = 0; i < 4; i++)
                plus5 &= shiftDir(plus5, a);
            //erode and dilate to get plus5 without lines exactly 5
            long plus6 = plus5 & shiftDir(plus5, a);
            plus6 |= shiftDir(plus6, a+4);
            if((plus5 & ~plus6) != 0)//exactly 5
                return true;
        }
        return false;
    }

    @Override
    public int checkWinner(SimpleBoard board) {
        if(checkWin(board.get(board.turn ^ 1)))//player who just went
            return board.turn^1;

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
