package gpuproj.srctree;

public class ArrayTypeSymbol extends TypeSymbol
{
    public final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.fullname+"[]");
        this.type = type;
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
