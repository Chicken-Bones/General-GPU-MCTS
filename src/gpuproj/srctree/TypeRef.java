package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class TypeRef
{
    public final TypeSymbol type;
    public List<TypeRef> params = new LinkedList<>();

    public TypeRef(TypeSymbol type) {
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
        sb.append(type.fullname);
        if(!params.isEmpty()) {
            sb.append('<');
            for(int i = 0; i < params.size(); i++) {
                if(i > 0) sb.append(", ");
                sb.append(params.get(i));
            }
            sb.append('>');
        }

        return sb.toString();
    }
}
