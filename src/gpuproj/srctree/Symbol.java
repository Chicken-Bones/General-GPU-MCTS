package gpuproj.srctree;

/**
 * Parent for package/class/method/field
 */
public abstract class Symbol
{
    public static final int PACKAGE_SYM = 1;
    public static final int CLASS_SYM = 2;
    public static final int FIELD_SYM = 4;
    public static final int METHOD_SYM = 8;
    public static final int INNER_SYM = 0x10;

    /**
     * Fully qualified name of this symbol
     */
    public final String name;
    /**
     * Containing class for fields/methods, outer class for inner classes, package for classes
     */
    public final String owner;

    public Symbol(String name) {
        this.name = name;
        owner = SourceUtil.parentName(name);
    }
}
