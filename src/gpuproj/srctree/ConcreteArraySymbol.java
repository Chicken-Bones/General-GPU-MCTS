package gpuproj.srctree;

import java.util.Arrays;
import java.util.List;

public class ConcreteArraySymbol extends ConcreteTypeSymbol
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
    public int getType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public List<FieldSymbol> getFields() {
        return Arrays.asList(length);
    }

    @Override
    public String toString() {
        return printName();
    }
}
