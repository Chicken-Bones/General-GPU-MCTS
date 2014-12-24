package gpuproj.srctree;

public class FieldSymbol extends Symbol
{
    public final String declaration;
    public TypeRef type;
    public int modifiers;
    public Expression initialiser;

    public FieldSymbol(String fullname, String declaration) {
        super(fullname);
        this.declaration = declaration;
    }

    public static FieldSymbol fromStatement(String parent, String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        r.readModifiers();
        r.readElement();//type
        r.skipTypeParams();
        return new FieldSymbol(SourceUtil.combineName(parent, r.readElement()), stmt);
    }
}
