package gpuproj.srctree;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MethodSymbol implements ParameterisableSymbol, AnnotatedSymbol
{
    public final Object source;
    public final Scope scope;
    private AnnotatedElement runtimeMethod;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<TypeParam>();
    public TypeRef returnType;
    public String fullname;
    public List<LocalSymbol> params = new LinkedList<LocalSymbol>();
    public boolean vaargs;
    public Statement body;

    public MethodSymbol(String fullname, Scope scope, Object source) {
        this.fullname = fullname;
        this.source = source;
        this.scope = new Scope(scope, this);
    }

    public MethodSymbol(String fullname, ClassSymbol owner, Object source) {
        this(fullname, owner.scope, source);
    }

    @Override
    public List<TypeParam> getTypeParams() {
        return typeParams;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    public AnnotatedElement runtimeMethod() {
        if(runtimeMethod == null) {
            Class<?> owner = owner().runtimeClass();
            Class[] paramTypes = new Class[params.size()];
            for(int i = 0; i < params.size(); i++)
                paramTypes[i] = params.get(i).getType().type.runtimeClass();
            try {
                runtimeMethod = getName().equals("<init>") ?
                        owner.getDeclaredConstructor(paramTypes) :
                        owner.getDeclaredMethod(getName(), paramTypes);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return runtimeMethod;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> type) {
        return runtimeMethod().getAnnotation(type);
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if(type == TYPE_PARAM) {
            for(TypeParam p : typeParams)
                if(p.getName().equals(name))
                    list.add(p);
        }
        if(type == LOCAL_SYM) {
            for(LocalSymbol p : params)
                if(p.name.equals(name))
                    list.add(p);
        }
    }

    @Override
    public int symbolType() {
        return Symbol.METHOD_SYM;
    }

    @Override
    public String toString() {
        if(returnType != null) {
            StringBuilder sb = new StringBuilder();
            if(modifiers != 0)
                sb.append(Modifier.toString(modifiers)).append(' ');
            if(!typeParams.isEmpty())
                sb.append("<").append(SourceUtil.listString(typeParams)).append("> ");
            sb.append(returnType).append(' ').append(fullname);
            sb.append('(').append(SourceUtil.listString(params)).append(')');
            return sb.toString();
        }

        return fullname;
    }

    public String signature() {
        if(returnType == null)//not fully initialised
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (LocalSymbol param : params)
            sb.append(param.type.signature());
        sb.append(')');
        sb.append(returnType.signature());
        return sb.toString();
    }

    public boolean matches(List paramTypes) {
        //TODO support vaargs
        if(params.size() != paramTypes.size())
            return false;

        for(int i = 0; i < params.size(); i++)
            if(!TypeRef.get(paramTypes.get(i)).concrete().isAssignableTo(params.get(i).type.concrete()))
                return false;

        return true;
    }

    /**
     * @return true if this is more specific than method, (any arguments passed to this could be passed to method)
     */
    public boolean isMoreSpecific(MethodSymbol method) {
        return method.matches(params);
    }

    public static MethodSymbol match(List<MethodSymbol> methods, List paramTypes) {
        List<MethodSymbol> matching = new LinkedList<MethodSymbol>();
        for(MethodSymbol m : methods)
            if(m.matches(paramTypes))
                matching.add(m);

        if(matching.isEmpty())
            return null;

        if(matching.size() == 1)
            return matching.get(0);

        Collections.sort(matching, new Comparator<MethodSymbol>()
        {
            @Override
            public int compare(MethodSymbol o1, MethodSymbol o2) {
                if(o1.isMoreSpecific(o2))
                    return -1;
                if(o2.isMoreSpecific(o1))
                    return 1;

                return 0;
            }
        });

        return matching.get(0);
    }

    public void loadBody() {
        if(!(source instanceof String))
            throw new IllegalArgumentException("Cannot load body for "+this+". Source not available");

        SourceReader r = new SourceReader((String) source);
        r.seekStart("{");
        body = (Block) r.readStatement(scope, false);
    }

    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }

    public String getName() {
        return SourceUtil.simpleName(fullname);
    }

    @Override
    public String globalName() {
        return fullname;
    }

    public String ownerName() {
        return SourceUtil.parentName(fullname);
    }

    public ClassSymbol owner() {
        return (ClassSymbol) TypeIndex.resolveType(ownerName());
    }

    public MethodSymbol copySig(String name, Scope scope, Object source) {
        MethodSymbol sym = new MethodSymbol(name, scope, source);
        sym.returnType = returnType;
        sym.params = new LinkedList<LocalSymbol>(params);
        sym.typeParams = new LinkedList<TypeParam>(typeParams);
        return sym;
    }
}
