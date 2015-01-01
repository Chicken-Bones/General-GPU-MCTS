package gpuproj.games;

import gpuproj.game.BoardGame;
import gpuproj.game.Move;
import gpuproj.util.Portable;
import gpuproj.game.SimpleBoard;

import java.util.ArrayList;
import java.util.List;
import static gpuproj.game.BitBoard.*;

public class Othello extends BoardGame<SimpleBoard>
{
    private long movementMask(long ply, long opp) {
        long empty = ~(ply|opp);
        long moves = 0;
        for(int d = 0; d < 8; d++) {
            long shift = shiftDir(ply, d);
            while(shift != 0) {
                shift = shiftDir(shift & opp, d);
                moves |= shift & empty;
            }
        }
        return moves;
    }

    private long flipMask(long move, long ply, long opp) {
        long flip = 0;
        for(int d = 0; d < 8; d++) {
            long shift = shiftDir(move, d) & opp;
            long line = 0;
            while(shift != 0) {
                line |= shift;
                shift = shiftDir(shift, d);
                if((shift & ply) != 0)
                    flip |= line;
                shift &= opp;
            }
        }
        return flip;
    }

    @Override
    public SimpleBoard getStartingBoard() {
        SimpleBoard board = new SimpleBoard();
        board.white = 0x0000000810000000L;
        board.black = 0x0000001008000000L;
        return board;
    }

    private ArrayList<SimpleBoard> moves = new ArrayList<>();
    @Override
    public List<? extends Move<SimpleBoard>> expand(SimpleBoard board) {
        moves.clear();

        long ply = board.get(board.turn);
        long opp = board.get(board.turn^1);
        long moveSet = movementMask(ply, opp);
        if(moveSet == 0) {
            SimpleBoard next = board.copy();
            next.turn ^= 1;
            moves.add(next);
            return moves;
        }

        while(moveSet != 0) {
            long move = moveSet & -moveSet;
            long flip = flipMask(move, ply, opp);

            SimpleBoard next = board.copy();
            next.set(board.turn, ply | move | flip);
            next.set(board.turn^1, opp & ~flip);
            next.turn ^= 1;
            moves.add(next);

            moveSet &= ~move;
        }

        return moves;
    }

    @Override
    public int checkWinner(SimpleBoard board) {
        if(movementMask(board.white, board.black) != 0 || movementMask(board.black, board.white) != 0)//a player can move
            return -1;//continue game

        return scoreWinner(board.white, board.black);
    }

    @Override
    public void playRandomMove(SimpleBoard board) {
        long ply = board.get(board.turn);
        long opp = board.get(board.turn^1);
        long moveSet = movementMask(ply, opp);
        if(moveSet == 0) {
            board.turn ^= 1;
            return;
        }

        int randmove = Portable.randInt(Long.bitCount(moveSet));
        for(int i = 0;; i++) {
            long move = moveSet & -moveSet;
            if (i == randmove) {
                long flip = flipMask(move, ply, opp);
                board.set(board.turn, ply | move | flip);
                board.set(board.turn ^ 1, opp & ~flip);
                board.turn ^= 1;
                break;
            }

            moveSet &= ~move;
        }
    }
}
