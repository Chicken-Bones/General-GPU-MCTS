package gpuproj.srctree;

import java.util.List;

/**
 * Class, primitive, or generic parameter
 */
public interface TypeSymbol extends Symbol
{
    /**
     * @return The a concrete type (reference, primitive, or array) that any valid instance of this symbol could be cast to
     */
    public abstract ConcreteTypeSymbol concrete();

    public abstract String printName();

    public abstract List<FieldSymbol> getFields();
}
