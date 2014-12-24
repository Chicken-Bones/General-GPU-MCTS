package gpuproj.srctree;

public class FieldSymbol extends Symbol
{
    public final Object source;
    public TypeRef type;
    public int modifiers;
    public Expression initialiser;

    public FieldSymbol(String fullname, Object source) {
        super(fullname);
        this.source = source;
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
