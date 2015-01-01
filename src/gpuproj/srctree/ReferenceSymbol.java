package gpuproj.srctree;

import java.util.List;

public abstract class ReferenceSymbol extends TypeSymbol
{
    public ReferenceSymbol(String fullname) {
        super(fullname);
    }

    public ReferenceSymbol concrete() {
        return this;
    }

    public FieldSymbol getField(String name) {
        if(!isConcrete())
            return concrete().getField(name);

        throw new UnsupportedOperationException();
    }

    public List<MethodSymbol> getMethods(String name) {
        if(!isConcrete())
            return concrete().getMethods(name);

        throw new UnsupportedOperationException();
    }
}
