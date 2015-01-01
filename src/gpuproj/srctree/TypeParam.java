package gpuproj.srctree;

public class TypeParam extends ReferenceSymbol
{
    /**
     * alias extends upper
     */
    public TypeRef upper;
    public ParameterisableSymbol owner;

    public TypeParam(String alias, TypeRef upper, ParameterisableSymbol owner) {
        super(alias);
        this.upper = upper;
        this.owner = owner;
    }

    public TypeParam(String alias, ParameterisableSymbol owner) {
        this(alias, new TypeRef(TypeIndex.instance().OBJECT), owner);
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
        if(upper.type != TypeIndex.instance().OBJECT)
            return fullname+" extends "+upper;

        return fullname;
    }
}
