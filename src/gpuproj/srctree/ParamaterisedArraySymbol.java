package gpuproj.srctree;

public class ParamaterisedArraySymbol implements ArraySymbol
{
    public final TypeSymbol type;
    public final FieldSymbol length;

    public ParamaterisedArraySymbol(TypeSymbol type) {
        this.type = type;
        length = new FieldSymbol(SourceUtil.combineName(printName(), "length"), null);
        length.type = new TypeRef(PrimitiveSymbol.INT);
    }

    @Override
    public int symbolType() {
        return TypeSymbol.TYPE_PARAM;
    }

    @Override
    public ConcreteTypeSymbol concrete() {
        return TypeIndex.instance().resolveType(type.concrete().fullname + "[]");
    }

    @Override
    public String printName() {
        return type.printName()+"[]";
    }

    @Override
    public String toString() {
        return printName();
    }

    @Override
    public TypeSymbol componentType() {
        return type;
    }

    public int dimension() {
        return (type instanceof ArraySymbol ? ((ArraySymbol) type).dimension() : 0) + 1;
    }
}
