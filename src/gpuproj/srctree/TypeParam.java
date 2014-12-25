package gpuproj.srctree;

public class TypeParam extends TypeSymbol
{
    /**
     * alias extends upper
     */
    public final TypeRef upper;

    public TypeParam(String alias, TypeRef upper) {
        super(alias);
        this.upper = upper;
    }

    public TypeParam(String alias) {
        this(alias, new TypeRef(TypeIndex.instance.resolveType("java.lang.Object")));
    }

    @Override
    public TypeSymbol concrete() {
        return upper.type.concrete();
    }

    @Override
    public int getType() {
        return Symbol.TYPE_PARAM;
    }

    @Override
    public String toString() {
        if(!upper.toString().equals("java.lang.Object"))
            return name+" extends "+upper;

        return name;
    }

    @Override
    public String signature() {
        return concrete().signature();
    }
}
