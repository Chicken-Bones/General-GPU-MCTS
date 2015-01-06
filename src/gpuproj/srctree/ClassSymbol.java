package gpuproj.srctree;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public abstract class ClassSymbol extends ReferenceSymbol implements ParameterisableSymbol, AnnotatedSymbol
{
    /**
     * extends Modifier
     */
    public static final int ANNOTATION = 0x00002000;
    /**
     * extends Modifier
     */
    public static final int ENUM = 0x00004000;
    /**
     * Symbols which are inherited from parent class/interface scopes
     */
    public static final int INHERITED_SYMS = Symbol.FIELD_SYM | Symbol.METHOD_SYM | Symbol.CLASS_SYM;

    public final Scope scope;
    /**
     * Object from which this symbol was constructed. Class for RuntimeClassSymbol, String for SourceClassSymbol
     */
    public final Object source;
    /**
     * True if this is an inner class
     */
    public boolean inner;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<>();
    /**
     * The parent type of this symbol, null if this is Object
     */
    public TypeRef parent;
    public List<TypeRef> interfaces = new LinkedList<>();
    public List<ClassSymbol> innerClasses = new LinkedList<>();
    public List<FieldSymbol> fields = new LinkedList<>();
    public List<MethodSymbol> methods = new LinkedList<>();

    public ClassSymbol(String fullname, Scope scope, Object source) {
        super(fullname);
        this.scope = new Scope(scope, this);
        this.source = source;
        TypeIndex.register(this);
    }

    public ClassSymbol load() {
        loadSymbols();
        loadSignatures();
        return this;
    }

    /**
     * Reads source to create field, method and inner class symbols by name.
     * The types, parameters of contents of thes symbols are not loaded until loadSignatures.
     * All contained symbols are loaded before any referenced types to prevent load cycles
     * Calls loadSymbols on inner classes
     * @return this
     */
    public abstract ClassSymbol loadSymbols();

    /**
     * Reads source to fill out parent class, interfaces, method and field signatures and type parameters, and inner classes
     * Calls loadSignatures on inner classes
     * @return this
     */
    public abstract ClassSymbol loadSignatures();

    /**
     * Sets inner to true
     * @return this
     */
    public ClassSymbol setInner() {
        inner = true;
        return this;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> type) {
        return runtimeClass().getAnnotation(type);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    /**
     * @return The name of the owner of this class, will be a valid package name, class name, or "" if this is declared in the root package
     */
    public String ownerName() {
        return SourceUtil.parentName(fullname);
    }

    /**
     * @return The ClassSymbol for the outer class if this is an inner class, otherwise null
     */
    public ClassSymbol owner() {
        return (ClassSymbol) TypeIndex.resolveType(ownerName());
    }

    @Override
    public String runtimeName() {
        return inner ? owner().runtimeName() + '$' + getName() : fullname;
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
    public List<TypeParam> getTypeParams() {
        return typeParams;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    private boolean shadowed(MethodSymbol method, List<Symbol> list) {
        for(Symbol sym : list)
            if(sym instanceof MethodSymbol && method.matches(((MethodSymbol) sym).params))
                return true;

        return false;
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if(type == FIELD_SYM && list.isEmpty()) {//accessors are shadowed by subclasses
            for(FieldSymbol sym : fields)
                if(sym.getName().equals(name))
                    list.add(sym);
        }
        if(type == METHOD_SYM) {
            boolean shadow = !list.isEmpty();
            for(MethodSymbol sym : methods)
                if(sym.getName().equals(name) && (!shadow || !shadowed(sym, list)))
                    list.add(sym);
        }
        if(type == CLASS_SYM) {
            for (ClassSymbol sym : innerClasses)
                if (sym.getName().equals(name))
                    list.add(sym);
        }
        if(type == TYPE_PARAM) {
            for(TypeParam p : typeParams)
                if(p.getName().equals(name))
                    list.add(p);
        }

        if((type & INHERITED_SYMS) != 0) {
            if (parent != null)
                parent.classType().resolveOnce(name, type, list);

            for (TypeRef iface : interfaces)
                iface.classType().resolveOnce(name, type, list);
        }
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
        if(parent != null && parent.type != TypeIndex.OBJECT)
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

    @Override
    public String signature() {
        return 'L'+fullname.replace('.', '/')+';';
    }

    /**
     * Finds the extends/implements reference for a given type in this class' type tree, to provide the generic arguments
     */
    public TypeRef parentRef(TypeSymbol type) {
        if(parent != null && parent.type == type)
            return parent;

        for(TypeRef iface : interfaces)
            if(iface.type == type)
                return iface;

        if(parent != null) {
            TypeRef ref = parent.classType().parentRef(type);
            if(ref != null)
                return ref;
        }

        for(TypeRef iface : interfaces) {
            TypeRef ref = iface.classType().parentRef(type);
            if(ref != null)
                return ref;
        }

        return null;
    }

    /**
     * A reference to this with all params
     */
    public TypeRef parameterPattern() {
        TypeRef ref = new TypeRef(this);
        for(TypeParam p : typeParams)
            ref.params.add(new TypeRef(p));

        return ref;
    }
}
