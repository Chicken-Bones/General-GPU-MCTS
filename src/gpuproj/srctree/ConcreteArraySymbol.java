package gpuproj.srctree;

import java.util.List;

public class ConcreteArraySymbol extends ConcreteTypeSymbol implements ArraySymbol
{
    public final TypeSymbol type;
    public final FieldSymbol length;

    public ConcreteArraySymbol(ConcreteTypeSymbol type) {
        super(type.fullname+"[]");
        this.type = type;
        length = new FieldSymbol(SourceUtil.combineName(fullname, "length"), null);
        length.type = new TypeRef(PrimitiveSymbol.INT);
        TypeIndex.instance().register(this);
    }

    @Override
    public int symbolType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String toString() {
        return printName();
    }

    @Override
    public boolean isAssignableTo(ConcreteTypeSymbol type) {
        return type == this;
    }

    @Override
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
