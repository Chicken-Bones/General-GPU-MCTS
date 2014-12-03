package gpuproj.games;

import gpuproj.game.Move;
import gpuproj.StructLike;

public class AtaxxMove2 implements Move<AtaxxBoard2>, StructLike<AtaxxMove2>
{
    /**
     * Coordinate of the piece being moved. (y<<3|x)
     */
    byte src;
    /**
     * Coordinate the destination. (y<<3|x)
     */
    byte dst;

    public AtaxxMove2() {}

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
