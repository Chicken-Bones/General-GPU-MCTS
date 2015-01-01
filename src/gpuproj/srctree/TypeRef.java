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

    public ReferenceSymbol refType() {
        return (ReferenceSymbol)type;
    }

    public ClassSymbol classType() {
        return (ClassSymbol) type;
    }

    public TypeRef componentType() {
        return new TypeRef(((ArraySymbol)type).componentType());
    }

    public TypeRef arrayType() {
        return new TypeRef(type.array());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.fullname);
        if(!params.isEmpty())
            sb.append('<').append(SourceUtil.listString(params)).append('>');

        return sb.toString();
    }

    public TypeSymbol concrete() {
        return type.concrete();
    }

    public static TypeRef get(Object o) {
        if(o instanceof TypeRef)
            return (TypeRef) o;
        if(o instanceof TypeSymbol)
            return new TypeRef((TypeSymbol) o);
        if(o instanceof Expression)
            return ((Expression) o).returnType();
        if(o instanceof Variable)
            return ((Variable) o).getType();
        if(o instanceof String)
            return new TypeRef(TypeIndex.instance().resolveType((String) o));

        return null;
    }

    public String signature() {
        return type.signature();
    }

    public TypeRef specify(TypeRef specifier) {
        if(specifier.params.isEmpty())
            return this;

        if(type instanceof TypeParam) {
            TypeParam param = (TypeParam) type;
            if(param.owner == specifier.type)
                return specifier.params.get(param.owner.getTypeParams().indexOf(param));
        }

        return this;
    }
}
