package gpuproj.games;

import gpuproj.util.Portable;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;
import gpuproj.game.SimpleBoard;

import java.util.ArrayList;
import java.util.List;

import static gpuproj.game.BitBoard.*;

public class Clobber extends BoardGame<SimpleBoard>
{
    private long movementMask(long ply, long opp) {
        return dilate(ply) & opp;
    }

    @Override
    public SimpleBoard getStartingBoard() {
        SimpleBoard board = new SimpleBoard();
        board.white = 0x5555555555555555L;
        board.black = 0xAAAAAAAAAAAAAAAAL;
        return board;
    }

    private ArrayList<SimpleBoard> moves = new ArrayList<>();
    @Override
    public List<? extends Move<SimpleBoard>> expand(SimpleBoard board) {
        moves.clear();

        long ply = board.get(board.turn);
        long opp = board.get(board.turn^1);
        long moveSet = movementMask(ply, opp);

        while(moveSet != 0) {
            long move = moveSet & -moveSet;
            long pieces = movementMask(move, ply);//pieces that can make this move
            while(pieces != 0) {
                long piece = pieces & -pieces;

                SimpleBoard next = board.copy();
                next.set(board.turn, ply & ~piece | move);
                next.set(board.turn^1, opp & ~move);
                next.turn ^= 1;
                moves.add(next);

                pieces &= ~piece;
            }

            moveSet &= ~move;
        }

        return moves;
    }

    @Override
    public int checkWinner(SimpleBoard board) {
        if(movementMask(board.get(board.turn), board.get(board.turn^1)) == 0)//player cannot move
            return board.turn^1;//opponent wins

        return -1;//continue game
    }

    @Override
    public void playRandomMove(SimpleBoard board) {
        long ply = board.get(board.turn);
        long opp = board.get(board.turn^1);
        long moveSet = movementMask(ply, opp);

        int randmove = Portable.randInt(Long.bitCount(moveSet));
        for(int i = 0;; i++) {
            long move = moveSet & -moveSet;
            if (i == randmove) {
                long pieces = movementMask(move, ply);//pieces that can make this move
                int randpiece = Portable.randInt(Long.bitCount(pieces));
                for(int j = 0;; j++) {
                    long piece = pieces & -pieces;
                    if (j == randpiece) {
                        board.set(board.turn, ply & ~piece | move);
                        board.set(board.turn^1, opp & ~move);
                        board.turn ^= 1;
                        return;
                    }

                    pieces &= ~piece;
                }
            }

            moveSet &= ~move;
        }
    }
}
