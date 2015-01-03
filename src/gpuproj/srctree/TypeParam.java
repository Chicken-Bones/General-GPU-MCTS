package gpuproj.srctree;

/**
 * A generic type parameter declaration on a ParamaterisableSymbol
 */
public class TypeParam extends ReferenceSymbol
{
    /**
     * The upper bound for this param (T extends upper) or Object if none specified
     */
    public TypeRef upper;
    /**
     * The symbol which declared this
     */
    public ParameterisableSymbol owner;

    public TypeParam(String alias, TypeRef upper, ParameterisableSymbol owner) {
        super(alias);
        this.upper = upper;
        this.owner = owner;
    }

    public TypeParam(String alias, ParameterisableSymbol owner) {
        this(alias, new TypeRef(TypeIndex.OBJECT), owner);
    }

    @Override
    public boolean isConcrete() {
        return false;
    }

    @Override
    public ReferenceSymbol concrete() {
        return (ReferenceSymbol) upper.concrete();
    }

    @Override
    public String signature() {
        return concrete().signature();
    }

    @Override
    public int symbolType() {
        return Symbol.TYPE_PARAM;
    }

    @Override
    public String toString() {
        if(upper.type != TypeIndex.OBJECT)
            return fullname+" extends "+upper;

        return fullname;
    }
}
