package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class FieldSymbol extends GlobalSymbol implements Variable
{
    public final Object source;
    public List<AnnotationSymbol> annotations = new LinkedList<>();
    public int modifiers;
    public TypeRef type;
    public Expression initialiser;

    public FieldSymbol(String fullname, Object source) {
        super(fullname);
        this.source = source;
    }

    @Override
    public String toString() {
        if(type != null)
            return (modifiers != 0 ? Modifier.toString(modifiers) + ' ' : "") + type + ' ' + name;

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
    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }
}
