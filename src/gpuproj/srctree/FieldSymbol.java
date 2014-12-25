package gpuproj.srctree;

import java.lang.reflect.Modifier;

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

    @Override
    public String toString() {
        if(type != null)
            return (modifiers != 0 ? Modifier.toString(modifiers) + ' ' : "") + type + ' ' + name;

        return fullname;
    }

    @Override
    public int getType() {
        return Symbol.FIELD_SYM;
    }
}
