package gpuproj.srctree;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldSymbol implements Variable, AnnotatedSymbol
{
    public final Object source;
    public final Scope scope;
    private Field runtimeField;
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

    public Field runtimeField() {
        if(runtimeField == null) {
            Class<?> owner = owner().runtimeClass();
            try {
                runtimeField = owner.getDeclaredField(getName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        return runtimeField;
    }

    @Override
    public Annotation getAnnotation(Class<? extends Annotation> type) {
        return runtimeField().getAnnotation(type);
    }

    @Override
    public String toString() {
        if(type != null)
            return (modifiers != 0 ? Modifier.toString(modifiers) + ' ' : "") + type + ' ' + fullname;

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
            r.seekStart("=");
            if(!r.end()) {
                r.readElement();
                init = r.readExpression(scope);
                return;
            }
        }

        init = type.type.defaultValue();
    }

    public String ownerName() {
        return SourceUtil.parentName(fullname);
    }

    public ClassSymbol owner() {
        return (ClassSymbol) TypeIndex.instance().resolveType(ownerName());
    }
}
