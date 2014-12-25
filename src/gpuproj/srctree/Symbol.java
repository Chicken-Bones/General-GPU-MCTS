package gpuproj.srctree;

/**
 * Parent for package/class/method/field
 */
public abstract class Symbol
{
    public static final int CLASS_SYM = 1;
    public static final int FIELD_SYM = 2;
    public static final int METHOD_SYM = 4;
    public static final int TYPE_PARAM = 8;
    public static final int TYPE_SYM = CLASS_SYM | TYPE_PARAM;
    public static final int TYPE_COUNT = 4;

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

    /**
     * @return One of CLASS_SYM, FIELD_SYM, METHOD_SYM or TYPE_PARAM
     */
    public abstract int getType();
}
