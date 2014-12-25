package gpuproj.srctree;

public class ArrayTypeSymbol extends ReferenceSymbol
{
    public final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.fullname+"[]", TypeIndex.instance.scope, null);
        this.type = type;
    }

    @Override
    public ReferenceSymbol loadSymbols() {
        return this;
    }

    @Override
    public ReferenceSymbol loadSignatures() {
        FieldSymbol length = new FieldSymbol(SourceUtil.combineName(fullname, "length"), null);
        length.type = new TypeRef(PrimitiveSymbol.INT);
        fields.add(length);
        return this;
    }

    @Override
    public TypeSymbol concrete() {
        return this;
    }

    @Override
    public String signature() {
        return '['+type.signature();
    }

    @Override
    public int getType() {
        return 0;
    }
}
