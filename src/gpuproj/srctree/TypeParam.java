package gpuproj.srctree;

public class TypeParam extends ReferenceSymbol
{
    /**
     * alias extends upper
     */
    public TypeRef upper;

    public TypeParam(String alias, TypeRef upper) {
        super(alias);
        this.upper = upper;
    }

    public TypeParam(String alias) {
        this(alias, new TypeRef(TypeIndex.instance().OBJECT));
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
