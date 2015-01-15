package gpuproj.srctree;

import java.lang.reflect.*;

public class RuntimeClassSymbol extends ClassSymbol
{
    public RuntimeClassSymbol(Scope scope, Class<?> c) {
        super(c.getCanonicalName(), scope, c);
    }

    @Override
    public RuntimeClassSymbol loadSymbols() {
        Class<?> c = (Class)source;
        modifiers = c.getModifiers();
        for(Class<?> inner : c.getDeclaredClasses())
            if(!inner.isAnonymousClass())
                innerClasses.add(new RuntimeClassSymbol(scope, inner).loadSymbols().setInner());
        for(Field f : c.getDeclaredFields())
            fields.add(new FieldSymbol(SourceUtil.combineName(fullname, f.getName()), this, f));
        for(Constructor<?> m : c.getDeclaredConstructors())
            methods.add(new MethodSymbol(SourceUtil.combineName(fullname, "<init>"), this, m));
        for(Method m : c.getDeclaredMethods())
            methods.add(new MethodSymbol(SourceUtil.combineName(fullname, m.getName()), this, m));

        return this;
    }

    @Override
    public RuntimeClassSymbol loadSignatures() {
        Class<?> c = (Class)source;
        loadTypeParams(this, c);

        Type p = c.getGenericSuperclass();
        if(p != null)
            parent = loadTypeRef(scope, p);

        for(Type t : c.getGenericInterfaces())
            interfaces.add(loadTypeRef(scope, t));

        for (ClassSymbol inner : innerClasses)
            inner.loadSignatures();

        for (FieldSymbol f : fields)
            loadSignature(f);

        for (MethodSymbol m : methods)
            loadSignature(m);

        return this;
    }

    private void loadSignature(FieldSymbol fsym) {
        Field f = (Field) fsym.source;
        fsym.modifiers = f.getModifiers();
        fsym.type = loadTypeRef(scope, f.getGenericType());
    }

    private void loadSignature(MethodSymbol msym) {
        if(msym.source instanceof Constructor) {
            Constructor c = (Constructor) msym.source;
            msym.modifiers = c.getModifiers();
            loadTypeParams(msym, c);
            msym.returnType = new TypeRef(this);
            for(Type t : c.getGenericParameterTypes()) {
                if(t instanceof Class && ((Class)t).isAnonymousClass())
                    continue;//generated out constructor reference param
                msym.params.add(new LocalSymbol(loadTypeRef(msym.scope, t), "arg"+msym.params.size()));
            }
        } else {
            Method m = (Method) msym.source;
            msym.modifiers = m.getModifiers();
            loadTypeParams(msym, m);
            msym.returnType = loadTypeRef(msym.scope, m.getGenericReturnType());
            for(Type t : m.getGenericParameterTypes())
                msym.params.add(new LocalSymbol(loadTypeRef(msym.scope, t), "arg"+msym.params.size()));
        }
    }

    private static void loadTypeParams(ParameterisableSymbol symbol, GenericDeclaration decl) {
        TypeVariable<?>[] generics;
        try {
            generics = decl.getTypeParameters();
        } catch (Exception e) {
            System.err.println("Warning, unable to get type parameters for "+symbol);
            e.printStackTrace();
            return;
        }

        for(TypeVariable<?> v : generics) {
            TypeParam p = new TypeParam(v.getName(), symbol);
            symbol.getTypeParams().add(p);
            p.upper = loadTypeRef(symbol.scope(), v.getBounds()[0]);
        }
    }

    private static TypeSymbol loadTypeSymbol(Scope scope, Type type) {
        if(type instanceof Class) {
            Class c = (Class) type;
            if(c.isArray())
                return loadTypeSymbol(scope, c.getComponentType()).array();
            if(c.isPrimitive())
                return PrimitiveSymbol.nameMap.get(c.getName());
            return (TypeSymbol) scope.resolve1(((Class) type).getCanonicalName(), Symbol.CLASS_SYM);
        }
        if(type instanceof TypeVariable)
            return (TypeSymbol) scope.resolve1(((TypeVariable) type).getName(), Symbol.TYPE_PARAM);
        if(type instanceof GenericArrayType)
            return loadTypeSymbol(scope, ((GenericArrayType) type).getGenericComponentType()).array();
        if(type instanceof WildcardType)
            return new TypeParam("?", loadTypeRef(scope, ((WildcardType) type).getUpperBounds()[0]), null);
        if(type instanceof ParameterizedType)//don't care about paramaterised types here, typically generic array creation
            return loadTypeSymbol(scope, ((ParameterizedType) type).getRawType());

        throw new IllegalArgumentException();
    }

    private static TypeRef loadTypeRef(Scope scope, Type type) {
        if(type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType)type;
            TypeRef r = new TypeRef(loadTypeSymbol(scope, pType.getRawType()));
            for(Type t : pType.getActualTypeArguments())
                r.params.add(loadTypeRef(scope, t));
            return r;
        }

        return new TypeRef(loadTypeSymbol(scope, type));
    }
}
