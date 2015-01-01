package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public abstract class ClassSymbol extends ReferenceSymbol implements ScopeProvider
{
    public static final int ANNOTATION = 0x00002000;
    public static final int ENUM = 0x00004000;
    public static final int INHERITED_SYMS = Symbol.FIELD_SYM | Symbol.METHOD_SYM | Symbol.CLASS_SYM;

    public final Scope scope;
    public final Object source;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<>();
    public TypeRef parent;
    public List<TypeRef> interfaces = new LinkedList<>();
    public List<ClassSymbol> innerClasses = new LinkedList<>();
    public List<FieldSymbol> fields = new LinkedList<>();
    public List<MethodSymbol> methods = new LinkedList<>();

    public ClassSymbol(String fullname, Scope scope, Object source) {
        super(fullname);
        this.scope = new Scope(scope, this);
        this.source = source;
        TypeIndex.instance().register(this);
    }

    public ClassSymbol load() {
        loadSymbols();
        loadSignatures();
        loadAnnotations();
        return this;
    }

    public abstract ClassSymbol loadSymbols();
    public abstract ClassSymbol loadSignatures();
    public abstract ClassSymbol loadAnnotations();

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public FieldSymbol getField(String name) {
        LinkedList<Symbol> list = new LinkedList<>();
        resolveOnce(name, FIELD_SYM, list);
        return list.isEmpty() ? null : (FieldSymbol)list.getFirst();
    }

    @Override
    public List<MethodSymbol> getMethods(String name) {
        LinkedList<Symbol> list = new LinkedList<>();
        resolveOnce(name, METHOD_SYM, list);
        return (List<MethodSymbol>)(List)list;
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if((type & FIELD_SYM) != 0 && list.isEmpty()) {//fields are shadowed by subclasses
            for(FieldSymbol sym : fields)
                if(sym.getName().equals(name))
                    list.add(sym);
        }
        if((type & METHOD_SYM) != 0) {
            for(MethodSymbol sym : methods)
                if(sym.getName().equals(name))
                    list.add(sym);
        }
        if((type & CLASS_SYM) != 0) {
            for (ClassSymbol sym : innerClasses)
                if (sym.getName().equals(name))
                    list.add(sym);
        }
        if((type & TYPE_PARAM) != 0) {
            for(TypeParam p : typeParams)
                if(p.getName().equals(name))
                    list.add(p);
        }

        if(parent != null)
            parent.classType().resolveOnce(name, type & INHERITED_SYMS, list);

        for(TypeRef iface : interfaces)
            iface.classType().resolveOnce(name, type & INHERITED_SYMS, list);
    }

    @Override
    public int symbolType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(modifiers != 0)
            sb.append(Modifier.toString(modifiers)).append(' ');

        if((modifiers & ENUM) != 0)
            sb.append("enum ");
        else if((modifiers & ANNOTATION) != 0)
            sb.append("@interface ");
        else if(!isInterface())
            sb.append("class ");

        sb.append(fullname);

        if(!typeParams.isEmpty())
            sb.append('<').append(SourceUtil.listString(typeParams)).append('>');
        if(parent != null && parent.type != TypeIndex.instance().OBJECT)
            sb.append(" extends ").append(parent);
        if(!interfaces.isEmpty())
            sb.append(" implements ").append(SourceUtil.listString(interfaces));

        return sb.toString();
    }

    public boolean isInterface() {
        return (modifiers & Modifier.INTERFACE) != 0;
    }

    @Override
    public boolean isAssignableTo(TypeSymbol type) {
        if(type == this)
            return true;

        if(parent != null && parent.type.isAssignableTo(type))
            return true;

        for(TypeRef iface : interfaces)
            if(iface.type.isAssignableTo(type))
                return true;

        return false;
    }
}
