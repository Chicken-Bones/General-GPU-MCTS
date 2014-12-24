package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.LinkedList;
import java.util.List;

public abstract class ReferenceSymbol extends TypeSymbol implements ScopeProvider
{
    public static final int ANNOTATION = 0x00002000;
    public static final int ENUM = 0x00004000;

    public final Scope scope;
    public final Object source;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<>();
    public TypeRef parent;
    public List<TypeRef> interfaces = new LinkedList<>();
    public List<ReferenceSymbol> innerClasses = new LinkedList<>();
    public List<FieldSymbol> fields = new LinkedList<>();
    public List<MethodSymbol> methods = new LinkedList<>();

    public ReferenceSymbol(String fullname, Scope scope, Object source) {
        super(fullname);
        this.scope = new Scope(scope, this);
        this.source = source;
        TypeIndex.instance.register(this);
    }

    public ReferenceSymbol load() {
        loadSymbols();
        loadSignatures();
        return this;
    }

    public abstract void loadSymbols();
    public abstract void loadSignatures();

    public FieldSymbol getField(String name) {
        return (FieldSymbol) resolveSingle(name, FIELD_SYM);
    }

    @Override
    public Symbol resolveSingle(String name, int type) {
        if((type & FIELD_SYM) != 0) {
            for(FieldSymbol sym : fields)
                if(sym.name.equals(name))
                    return sym;
        }
        if((type & METHOD_SYM) != 0) {
            for(MethodSymbol sym : methods)
                if(sym.name.equals(name))
                    return sym;
        }
        if((type & CLASS_SYM) != 0) {
            for (ReferenceSymbol sym : innerClasses)
                if (sym.name.equals(name))
                    return sym;
        }
        if((type & TYPEPARAM) != 0) {
            for(TypeParam p : typeParams)
                if(p.name.equals(name))
                    return p;
        }
        if(parent != null) {
            Symbol sym = parent.refType().resolveSingle(name, type);
            if(sym != null) return sym;
        }
        for(TypeRef iface : interfaces) {
            Symbol sym = iface.refType().resolveSingle(name, type);
            if(sym != null) return sym;
        }
        return null;
    }

    @Override
    public TypeSymbol concrete() {
        return this;
    }
}
