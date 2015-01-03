package gpuproj.srctree;

/**
 * Local variable or field
 */
public interface Variable extends Symbol
{
    /**
     * @return The type of this variable
     */
    public TypeRef getType();

    /**
     * @return The name by which this variable is declared
     */
    public String getName();

    /**
     * @return The expression this variable is initialised with. May be null for locals.
     */
    public Expression initialiser();
}
