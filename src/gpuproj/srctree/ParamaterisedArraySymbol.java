package gpuproj.srctree;

import java.util.Arrays;
import java.util.List;

public class ParamaterisedArraySymbol implements TypeSymbol
{
    public final TypeSymbol type;
    public final FieldSymbol length;

    public ParamaterisedArraySymbol(TypeSymbol type) {
        this.type = type;
        length = new FieldSymbol(SourceUtil.combineName(printName(), "length"), null);
        length.type = new TypeRef(PrimitiveSymbol.INT);
    }

    @Override
    public int getType() {
        return TypeSymbol.TYPE_PARAM;
    }

    @Override
    public List<FieldSymbol> getFields() {
        return Arrays.asList(length);
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
}
