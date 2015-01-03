package gpuproj.srctree;

/**
 * Interface for anything able to be resolved by name within a scope.
 * Only one symbol instance should exist for each physical source element
 */
public interface Symbol
{
    public static final int CLASS_SYM = 1; //ArraySymbol | PrimitiveSymbol | ClassSymbol
    public static final int FIELD_SYM = 2; //FieldSymbol
    public static final int METHOD_SYM = 4; //MethodSymbol
    public static final int TYPE_PARAM = 8; //TypeParam
    public static final int LOCAL_SYM = 0x10; //LocalSymbol
    public static final int LABEL = 0x20; //LabelledStatement
    public static final int VARIABLE = FIELD_SYM | LOCAL_SYM; //Variable
    public static final int TYPE_SYM = CLASS_SYM | TYPE_PARAM; //TypeSymbol
    public static final int TYPE_COUNT = 6;

    /**
     * @return One of CLASS_SYM, FIELD_SYM, METHOD_SYM, TYPE_PARAM, LOCAL_SYM or LABEL
     */
    public abstract int symbolType();

    /**
     * The unique name by which this symbol can be obtained from TypeIndex, or null if this is a type parameterised or local symbol
     */
    public abstract String globalName();
}
