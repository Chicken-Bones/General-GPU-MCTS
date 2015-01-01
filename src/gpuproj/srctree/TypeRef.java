package gpuproj.srctree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TypeRef
{
    public final TypeSymbol type;
    public List<TypeRef> params = new LinkedList<>();

    public TypeRef(TypeSymbol type) {
        if(type == null) throw new IllegalArgumentException("Null type");
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

    public static List<TypeRef> resolveList(String... typeNames) {
        List<TypeRef> types = new ArrayList<>(typeNames.length);
        for(String name : typeNames)
            types.add(new TypeRef(TypeIndex.instance().resolveType(name)));

        return types;
    }

    public TypeRef arrayType() {
        return new TypeRef(((ArraySymbol)type).componentType());
    }

    public static TypeRef get(Object o) {
        if(o instanceof TypeRef)
            return (TypeRef) o;
        if(o instanceof TypeSymbol)
            return new TypeRef((TypeSymbol) o);
        if(o instanceof Expression)
            return ((Expression) o).returnType();
        if(o instanceof String)
            return new TypeRef(TypeIndex.instance().resolveType((String) o));

        return null;
    }
}
