package gpuproj.games;

import gpuproj.game.BoardGame;
import gpuproj.game.MoveList;
import gpuproj.Portable;

import java.util.List;

public class Ataxx2 extends BoardGame<AtaxxBoard2>
{
    @Override
    public AtaxxBoard2 getStartingBoard() {
        AtaxxBoard2 board = new AtaxxBoard2();
        board.board[2][2] = -1;
        board.board[2][5] = -1;
        board.board[5][5] = -1;
        board.board[5][2] = -1;
        board.board[0][0] = 2;
        board.board[7][7] = 2;
        board.board[0][7] = 1;
        board.board[7][0] = 1;
        return board;
    }

    private final MoveList<AtaxxMove2> moves = new MoveList<AtaxxMove2>(200, AtaxxMove2.class);

    /**
     * Clears the move list, and adds all moves of b to it
     */
    private void genMoves(AtaxxBoard2 b) {
        moves.clear();
        int ply = b.turn + 1;

        for(int x = 0; x < 8; x++)
            for(int y = 0; y < 8; y++)
                if(b.board[x][y] == ply)
                    for(int i = Math.max(0, x-2); i <= Math.min(7, x+2); i++)
                        for(int j = Math.max(0, y-2); j <= Math.min(7, y+2); j++)
                            if(b.board[i][j] == 0)
                                moves.add(new AtaxxMove2(y<<3|x, j<<3|i));
    }

    @Override
    public List<AtaxxMove2> expand(AtaxxBoard2 board) {
        genMoves(board);
        return moves.toList();
    }

    @Override
    public void playRandomMove(AtaxxBoard2 board) {
        genMoves(board);
        moves.get(Portable.randInt(moves.size())).apply(board);
    }

    @Override
    public int checkWinner(AtaxxBoard2 b) {
        int ply = b.turn + 1;

        int white = 0;
        int black = 0;
        for(int x = 0; x < 8; x++)
            for(int y = 0; y < 8; y++) {
                switch (b.board[x][y]) {//calculate number of pieces
                    case 1: white++; break;
                    case 2: black++; break;
                    default: continue;//don't need following valid move check on blocked/empty squares
                }
                if(b.board[x][y] == ply)//check if there's a valid move
                    for(int i = Math.max(0, x-2); i <= Math.min(7, x+2); i++)
                        for(int j = Math.max(0, y-2); j <= Math.min(7, y+2); j++)
                            if(b.board[i][j] == 0)
                                return -1;//valid move found, game on
            }

        //determine winner based on score
        return white == black ? 2 : white > black ? 0 : 1;
    }
}
