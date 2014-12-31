package gpuproj.srctree;

import java.lang.reflect.*;
import java.util.List;

public class RuntimeClassSymbol extends ReferenceSymbol
{
    public RuntimeClassSymbol(Scope scope, Class<?> c) {
        super(c.getCanonicalName(), scope, c);
    }

    @Override
    public RuntimeClassSymbol loadSymbols() {
        Class<?> c = (Class<?>) source;
        modifiers = c.getModifiers();
        for(Class<?> inner : c.getDeclaredClasses())
            if(!inner.isAnonymousClass())
                innerClasses.add(new RuntimeClassSymbol(scope, inner).loadSymbols());
        for(Field f : c.getDeclaredFields())
            fields.add(new FieldSymbol(SourceUtil.combineName(fullname, f.getName()), f));
        for(Constructor<?> m : c.getDeclaredConstructors())
            methods.add(new MethodSymbol(SourceUtil.combineName(fullname, "<init>"), this, m));
        for(Method m : c.getDeclaredMethods())
            methods.add(new MethodSymbol(SourceUtil.combineName(fullname, m.getName()), this, m));

        return this;
    }

    @Override
    public RuntimeClassSymbol loadSignatures() {
        Class<?> c = (Class<?>) source;
        loadTypeParams(scope, c.getTypeParameters(), typeParams);

        Class<?> p = c.getSuperclass();
        if(p != null)
            parent = loadTypeRef(scope, p);

        for(Type t : c.getGenericInterfaces())
            interfaces.add(loadTypeRef(scope, t));

        for (ReferenceSymbol inner : innerClasses)
            inner.loadSignatures();

        for (FieldSymbol f : fields)
            loadSignature(f);

        for (MethodSymbol m : methods)
            loadSignature(m);

        return this;
    }

    @Override
    public ReferenceSymbol loadAnnotations() {
        //we don't care about compiled annotations at this time
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
            loadTypeParams(msym.scope, c.getTypeParameters(), msym.typeParams);
            msym.returnType = new TypeRef(PrimitiveSymbol.VOID);
            for(Type t : c.getGenericParameterTypes()) {
                if(t instanceof Class && ((Class)t).isAnonymousClass())
                    continue;//generated out constructor reference param
                msym.params.add(new LocalSymbol(loadTypeRef(msym.scope, t), "arg"+msym.params.size()));
            }
        } else {
            Method m = (Method) msym.source;
            msym.modifiers = m.getModifiers();
            loadTypeParams(msym.scope, m.getTypeParameters(), msym.typeParams);
            msym.returnType = loadTypeRef(msym.scope, m.getGenericReturnType());
            for(Type t : m.getGenericParameterTypes())
                msym.params.add(new LocalSymbol(loadTypeRef(msym.scope, t), "arg"+msym.params.size()));
        }
    }

    private static void loadTypeParams(Scope scope, TypeVariable<?>[] generics, List<TypeParam> params) {
        for(TypeVariable<?> v : generics) {
            TypeParam p = new TypeParam(v.getName());
            params.add(p);
            p.upper = loadTypeRef(scope, v.getBounds()[0]);
        }
    }

    private static TypeSymbol loadTypeSymbol(Scope scope, Type type) {
        if(type instanceof Class) {
            Class c = (Class) type;
            if(c.isArray())
                return new ConcreteArraySymbol((ConcreteTypeSymbol) loadTypeSymbol(scope, c.getComponentType()));
            if(c.isPrimitive())
                return PrimitiveSymbol.nameMap.get(c.getName());
            return (TypeSymbol) scope.resolve1(((Class) type).getCanonicalName(), Symbol.CLASS_SYM);
        }
        if(type instanceof TypeVariable)
            return (TypeSymbol) scope.resolve1(((TypeVariable) type).getName(), Symbol.TYPE_PARAM);
        if(type instanceof GenericArrayType)
            return new ParamaterisedArraySymbol(loadTypeSymbol(scope, ((GenericArrayType) type).getGenericComponentType()));
        if(type instanceof WildcardType)
            return new TypeParam("?", loadTypeRef(scope, ((WildcardType) type).getUpperBounds()[0]));
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
