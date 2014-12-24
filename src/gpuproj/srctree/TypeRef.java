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
}
