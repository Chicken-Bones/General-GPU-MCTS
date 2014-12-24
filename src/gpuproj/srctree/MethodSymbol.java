package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class MethodSymbol extends Symbol
{
    public final String declaration;
    public ClassSymbol owner;
    public int modifiers;
    public TypeRef returnType;
    public List<LocalSymbol> params = new LinkedList<>();
    public Block body;

    public MethodSymbol(String fullname, String declaration) {
        super(fullname);
        this.declaration = declaration;
    }

    public static MethodSymbol fromStatement(String parent, String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        r.readModifiers();
        r.skipTypeParams();//method type aliases
        r.readElement();//return type
        r.skipTypeParams();//return type params
        return new MethodSymbol(SourceUtil.combineName(parent, r.readElement()), stmt);
    }
}
