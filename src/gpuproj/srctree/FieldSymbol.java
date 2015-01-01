package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class FieldSymbol implements Variable
{
    public final Object source;
    public final Scope scope;
    public List<AnnotationSymbol> annotations = new LinkedList<>();
    public int modifiers;
    public TypeRef type;
    public String fullname;
    public Expression init;

    public FieldSymbol(String fullname, Scope scope, Object source) {
        this.fullname = fullname;
        this.source = source;
        this.scope = scope;
    }

    public FieldSymbol(String fullname, ClassSymbol owner, Object source) {
        this(fullname, owner.scope, source);
    }

    public FieldSymbol(String fullname) {
        this(fullname, (Scope)null, null);
    }

    @Override
    public String toString() {
        if(type != null)
            return (modifiers != 0 ? Modifier.toString(modifiers) + ' ' : "") + type + ' ' + getName();

        return fullname;
    }

    @Override
    public int symbolType() {
        return Symbol.FIELD_SYM;
    }

    @Override
    public TypeRef getType() {
        return type;
    }

    @Override
    public String globalName() {
        return fullname;
    }

    @Override
    public String getName() {
        return SourceUtil.simpleName(fullname);
    }

    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }

    public void loadInitialiser() {
        if(source != null) {
            SourceReader r = new SourceReader((String) source);
            r.skipAnnotations();
            if(r.indexOf('=') > 0) {
                r.seek("=");
                r.readElement();//=
                init = r.readExpression(scope);
                return;
            }
        }

        init = type.type.defaultValue();
    }
}
