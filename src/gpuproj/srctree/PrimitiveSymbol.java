package gpuproj.srctree;

/**
 * one of byte, short, char, int, long, float, double, boolean, void
 */
public class PrimitiveSymbol extends TypeSymbol
{
    public final String signature;

    public PrimitiveSymbol(String fullname, String signature) {
        super(fullname);
        this.signature = signature;
    }

    @Override
    public TypeSymbol concrete() {
        return this;
    }

    @Override
    public String signature() {
        return signature;
    }
}
