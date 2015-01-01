package gpuproj.srctree;

public interface ArraySymbol extends TypeSymbol
{
    public TypeSymbol componentType();

    public int dimension();
}
