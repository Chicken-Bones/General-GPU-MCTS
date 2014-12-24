package gpuproj.srctree;

public abstract class ReferenceSymbol extends TypeSymbol
{
    public ReferenceSymbol(String fullname) {
        super(fullname);
    }

    public abstract FieldSymbol getField(String member);
}
