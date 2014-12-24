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
    public static final int TYPEPARAM = 0x10;
    public static final int TYPE_SYM = CLASS_SYM | TYPEPARAM;

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

    public Symbol(String fullname) {
        this.fullname = fullname;
        name = SourceUtil.simpleName(fullname);
        owner = SourceUtil.parentName(fullname);
    }
}
