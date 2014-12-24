package gpuproj.srctree;

public class TypeParam extends TypeSymbol
{
    /**
     * alias extends upper
     */
    public TypeRef upper = new TypeRef(TypeIndex.instance.resolveType("java.lang.Object"));

    public TypeParam(String fullname) {
        super(fullname);
    }

    @Override
    public TypeSymbol concrete() {
        return upper.type.concrete();
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
