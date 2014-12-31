package gpuproj.srctree;

public abstract class ConcreteTypeSymbol extends GlobalSymbol implements TypeSymbol
{
    public ConcreteTypeSymbol(String fullname) {
        super(fullname);
    }

    @Override
    public ConcreteTypeSymbol concrete() {
        return this;
    }

    @Override
    public String printName() {
        return fullname;
    }
}
