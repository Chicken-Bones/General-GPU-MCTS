package gpuproj.srctree;

public abstract class GlobalSymbol implements Symbol
{
    /**
     * Simple name of this symbol, after the last '.'
     */
    public final String name;
    /**
     * Fully qualified name of this symbol
     */
    public final String fullname;
    /**
     * Containing class for fields/methods, outer class for inner classes, package for classes
     */
    public final String owner;

    public GlobalSymbol(String fullname) {
        this.fullname = fullname;
        name = SourceUtil.simpleName(fullname);
        owner = SourceUtil.parentName(fullname);
    }
}
