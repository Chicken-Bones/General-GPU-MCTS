package gpuproj.games;

import gpuproj.game.AbstractSimpleBoard;

import static gpuproj.game.BitBoard.*;

/**
 * 8x8 ataxx board
 */
public final class AtaxxBoard extends AbstractSimpleBoard<AtaxxBoard>
{
    public static final long blocked = 0x0000240000240000L;

    @Override
    public AtaxxBoard copy() {
        return new AtaxxBoard().set(this);
    }

    @Override
    protected String format() {
        return overlay(super.format(), format8(blocked, 'x'));
    }
}
