package gpuproj.srctree;

import java.util.List;

public abstract class ReferenceSymbol extends TypeSymbol
{
    private Class<?> runtimeClass;

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

    @Override
    public String runtimeName() {
        if(!isConcrete())
            return concrete().runtimeName();

        throw new UnsupportedOperationException();
    }

    public Class<?> runtimeClass() {
        try {
            if(runtimeClass == null)
                runtimeClass = getClass().getClassLoader().loadClass(runtimeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return runtimeClass;
    }
}
