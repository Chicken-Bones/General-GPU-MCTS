package gpuproj.games;

import gpuproj.game.BoardGame;
import gpuproj.Portable;

import java.util.ArrayList;
import java.util.List;
import static gpuproj.game.BitBoard.*;

public class Ataxx extends BoardGame<AtaxxBoard>
{
    private long movementMask(long pieces) {
        return dilate8(dilate8(pieces));
    }

    private long attackMask(long pieces) {
        return dilate8(pieces);
    }

    @Override
    public AtaxxBoard getStartingBoard() {
        AtaxxBoard board = new AtaxxBoard();
        board.black = 0x8000000000000001L;
        board.white = 0x0100000000000080L;
        return board;
    }

    private ArrayList<AtaxxBoard> moves = new ArrayList<AtaxxBoard>();
    @Override
    public List<AtaxxBoard> expand(AtaxxBoard board) {
        moves.clear();

        long ply = board.turn == 0 ? board.white : board.black;
        if(ply == 0) return moves;

        long opp = board.turn == 0 ? board.black : board.white;
        long empty = ~(ply|opp|AtaxxBoard.blocked);
        long pieces = ply;
        while(pieces != 0) {
            long piece = pieces & -pieces;
            long dup = attackMask(piece);
            long moveSet = movementMask(piece) & empty;
            while(moveSet != 0) {
                long move = moveSet & -moveSet;
                long attack = attackMask(move);
                long ply2 = ply | move | opp & attack;
                long opp2 = opp & ~attack;
                if((move & dup) == 0) ply2 &= ~piece;

                AtaxxBoard next = board.copy();
                next.set(board.turn, ply2);
                next.set(board.turn^1, opp2);
                next.turn ^= 1;
                moves.add(next);

                moveSet &= ~move;
            }
            pieces &= ~piece;
        }

        return moves;
    }

    @Override
    public int checkWinner(AtaxxBoard board) {
        if(board.black == 0) return 0;//black eliminated
        if(board.white == 0) return 1;//white eliminated
        long empty = ~(board.black|board.white|AtaxxBoard.blocked);
        if ((movementMask(board.get(board.getTurn())) & empty) != 0)//player can move
            return -1;//continue game

        return scoreWinner(board.white, board.black);
    }

    @Override
    public void playRandomMove(AtaxxBoard board) {
        long ply = board.turn == 0 ? board.white : board.black;
        long opp = board.turn == 0 ? board.black : board.white;
        long empty = ~(ply|opp|AtaxxBoard.blocked);
        long moveSet = movementMask(ply) & empty;//valid move destinations

        int randmove = Portable.randInt(Long.bitCount(moveSet));
        for(int i = 0;; i++) {
            long move = moveSet & -moveSet;
            if (i == randmove) {
                long pieces = movementMask(move) & ~attackMask(move) & ply;//non duplicating source pieces

                //apply move and attack
                long attack = attackMask(move);
                ply |= move | opp & attack;
                opp &= ~attack;

                int randpiece = Portable.randInt(Long.bitCount(pieces) + 1);
                if(randpiece > 0) {//0 is a duplicating move. Otherwise find the source piece and remove it
                    for(int j = 1;; j++) {
                        long piece = pieces & -pieces;
                        if(j == randpiece) {
                            ply &= ~piece;
                            break;
                        }

                        pieces &= ~piece;
                    }
                }
                break;
            }

            moveSet &= ~move;
        }

        board.set(board.turn, ply);
        board.set(board.turn^1, opp);
        board.turn ^= 1;
    }
}
