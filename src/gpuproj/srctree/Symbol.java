package gpuproj.srctree;

/**
 * Parent for package/class/method/field
 */
public interface Symbol
{
    public static final int CLASS_SYM = 1; //ConcreteTypeSymbol
    public static final int FIELD_SYM = 2; //FieldSymbol
    public static final int METHOD_SYM = 4; //MethodSymbol
    public static final int TYPE_PARAM = 8;
    public static final int LOCAL_SYM = 0x10;
    public static final int VARIABLE = FIELD_SYM | LOCAL_SYM;
    public static final int TYPE_SYM = CLASS_SYM | TYPE_PARAM;
    public static final int TYPE_COUNT = 5;

    /**
     * @return One of CLASS_SYM, FIELD_SYM, METHOD_SYM, TYPE_PARAM or LOCAL_SYM
     */
    public abstract int getType();
}
