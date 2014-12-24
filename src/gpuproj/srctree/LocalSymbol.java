package gpuproj.srctree;

public class LocalSymbol
{
    public final TypeRef type;
    public final String name;
    public Expression initialiser;

    public LocalSymbol(TypeRef type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type+" "+name;
    }
}
