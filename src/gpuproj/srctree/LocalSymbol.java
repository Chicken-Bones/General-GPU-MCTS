package gpuproj.srctree;

public class LocalSymbol extends Statement implements Symbol
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

    @Override
    public int getType() {
        return Symbol.LOCAL_SYM;
    }
}
