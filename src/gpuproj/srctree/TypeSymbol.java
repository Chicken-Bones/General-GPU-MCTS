package gpuproj.srctree;

/**
 * Class, primitive, or generic parameter
 */
public abstract class TypeSymbol extends Symbol
{
    public TypeSymbol(String fullname) {
        super(fullname);
    }

    /**
     * @return The a concrete type (reference, primitive, or array) that any valid instance of this symbol could be cast to
     */
    public abstract TypeSymbol concrete();
}
