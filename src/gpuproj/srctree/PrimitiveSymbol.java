package gpuproj.srctree;

/**
 * one of byte, short, char, int, long, float, double, boolean, void
 */
public class PrimitiveSymbol extends TypeSymbol
{
    public PrimitiveSymbol(String fullname) {
        super(fullname);
    }

    @Override
    public TypeSymbol concrete() {
        return this;
    }
}
