package gpuproj.games;

import gpuproj.StructLike;
import gpuproj.game.*;
import gpuproj.util.Portable;
import gpuproj.games.Ataxx2.AtaxxBoard2;

import java.util.List;

public class Ataxx2 extends BoardGame<AtaxxBoard2>
{
    public static class AtaxxBoard2 extends Board<AtaxxBoard2>
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

    public static class AtaxxMove2 implements Move<AtaxxBoard2>, StructLike<AtaxxMove2>
    {
        /**
         * Coordinate of the piece being moved. (y<<3|x)
         */
        byte src;
        /**
         * Coordinate the destination. (y<<3|x)
         */
        byte dst;

        public AtaxxMove2(int src, int dst) {
            this.src = (byte) src;
            this.dst = (byte) dst;
        }

        @Override
        public AtaxxBoard2 apply(AtaxxBoard2 b) {
            byte ply = (byte) (b.turn + 1);
            byte opp = (byte) ((b.turn^1) + 1);

            int dx = dst&7;
            int dy = dst>>3;

            b.board[dx][dy] = ply;
            if(Math.abs((src&7) - (dst&7)) == 2 || Math.abs((src>>3) - (dst>>3)) == 2)//2 spaces between src and dest
                b.board[src&7][src>>3] = 0;

            for(int i = Math.max(0, dx-1); i <= Math.min(7, dx+1); i++)
                for(int j = Math.max(0, dy-1); j <= Math.min(7, dy+1); j++)
                    if(b.board[i][j] == opp)
                        b.board[i][j] = ply;

            b.turn ^= 1;
            return b;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof AtaxxMove2)) return false;
            AtaxxMove2 m = (AtaxxMove2) obj;
            return src == m.src && dst == m.dst;
        }

        @Override
        public AtaxxMove2 set(AtaxxMove2 m) {
            src = m.src;
            dst = m.dst;
            return this;
        }

        @Override
        public AtaxxMove2 copy() {
            return new AtaxxMove2(src, dst);
        }

        @Override
        public String toString() {
            return ""+(src&7)+","+(src>>3)+" -> "+(dst&7)+","+(dst>>3);
        }
    }

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
        int ply = b.turn + 1;//LocalSymbol, exp (plus(member access, literal(1))

        int white = 0;
        int black = 0;
        for(int x = 0; x < 8; x++)//for
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
