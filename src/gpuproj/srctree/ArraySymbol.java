package gpuproj.srctree;

import java.util.List;

public class ArraySymbol extends ReferenceSymbol
{
    public final TypeSymbol type;
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

    public TypeSymbol componentType() {
        return type;
    }

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
    public List<MethodSymbol> getMethods(String name) {
        throw new UnsupportedOperationException();
    }
}
