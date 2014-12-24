package gpuproj.srctree;

public class PackageSymbol extends Symbol
{
    public PackageSymbol(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "package "+fullname;
    }
}
