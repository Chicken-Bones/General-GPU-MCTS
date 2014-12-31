package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class TypeRef
{
    public final TypeSymbol type;
    public List<TypeRef> params = new LinkedList<>();

    public TypeRef(TypeSymbol type) {
        assert type != null;
        this.type = type;
    }

    /**
     * Simple getter for type as a ReferenceSymbol
     * @return type casted to ReferenceSymbol
     */
    public ReferenceSymbol refType() {
        return (ReferenceSymbol)type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.printName());
        if(!params.isEmpty())
            sb.append('<').append(SourceUtil.listString(params)).append('>');

        return sb.toString();
    }

    public ConcreteTypeSymbol concrete() {
        return type.concrete();
    }
}
