package gpuproj.game;

/**
 * Concrete subclass of AbstractSimpleBoard
 */
public class SimpleBoard extends AbstractSimpleBoard<SimpleBoard>
{
    @Override
    public SimpleBoard copy() {
        return new SimpleBoard().set(this);
    }
}
