package gpuproj.srctree;

/**
 * Parent for package/class/method/field
 */
public interface Symbol
{
    public static final int CLASS_SYM = 1; //ConcreteTypeSymbol
    public static final int FIELD_SYM = 2; //FieldSymbol
    public static final int METHOD_SYM = 4; //MethodSymbol
    public static final int TYPE_PARAM = 8; //TypeParam
    public static final int LOCAL_SYM = 0x10; //LocalSymbol
    public static final int LABEL = 0x20; //LabelledStatement
    public static final int VARIABLE = FIELD_SYM | LOCAL_SYM; //Variable
    public static final int TYPE_SYM = CLASS_SYM | TYPE_PARAM; //TypeSymbol
    public static final int TYPE_COUNT = 6;

    /**
     * @return One of CLASS_SYM, FIELD_SYM, METHOD_SYM, TYPE_PARAM or LOCAL_SYM
     */
    public abstract int symbolType();

    /**
     * The unique name by which this symbol can be obtained from TypeIndex, or null if this is a parameterised class
     */
    public abstract String globalName();
}
