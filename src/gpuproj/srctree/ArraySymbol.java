package gpuproj.srctree;

import java.util.List;

/**
 * Symbol representing array types
 */
public class ArraySymbol extends ReferenceSymbol
{
    /**
     * The component type of this array
     */
    public final TypeSymbol type;
    /**
     * The length field
     */
    public final FieldSymbol length;

    public ArraySymbol(TypeSymbol type) {
        super(type.fullname+"[]");
        this.type = type;
        length = new FieldSymbol(SourceUtil.combineName(fullname, "length"));
        length.type = new TypeRef(PrimitiveSymbol.INT);
    }

    @Override
    public int symbolType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String toString() {
        return fullname;
    }

    @Override
    public boolean isConcrete() {
        return type.isConcrete();
    }

    @Override
    public ArraySymbol concrete() {
        return isConcrete() ? this : type.concrete().array();
    }

    @Override
    public String signature() {
        return '['+type.signature();
    }

    /**
     * @return The dimension of this array, calculated recursively.
     */
    public int dimension() {
        return (type instanceof ArraySymbol ? ((ArraySymbol) type).dimension() : 0) + 1;
    }

    @Override
    public FieldSymbol getField(String name) {
        if(name.equals("length"))
            return length;

        return null;
    }

    @Override
    public String runtimeName() {
        return type.runtimeName()+"[]";
    }

    @Override
    public List<MethodSymbol> getMethods(String name) {
        throw new UnsupportedOperationException();
    }
}
