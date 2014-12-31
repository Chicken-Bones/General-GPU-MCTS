package gpuproj.srctree;

import java.util.List;

public class TypeParam implements TypeSymbol
{
    public final String alias;
    /**
     * alias extends upper
     */
    public TypeRef upper;

    public TypeParam(String alias, TypeRef upper) {
        this.alias = alias;
        this.upper = upper;
    }

    public TypeParam(String alias) {
        this(alias, new TypeRef(TypeIndex.instance().OBJECT));
    }

    @Override
    public ConcreteTypeSymbol concrete() {
        return upper.concrete();
    }

    @Override
    public List<FieldSymbol> getFields() {
        return concrete().getFields();
    }

    @Override
    public int getType() {
        return Symbol.TYPE_PARAM;
    }

    @Override
    public String toString() {
        if(upper.type != TypeIndex.instance().OBJECT)
            return alias+" extends "+upper;

        return alias;
    }

    @Override
    public String printName() {
        return alias;
    }
}
