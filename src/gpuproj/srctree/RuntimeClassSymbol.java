package gpuproj.srctree;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class RuntimeClassSymbol extends ReferenceSymbol
{
    public RuntimeClassSymbol(Scope scope, Class<?> c) {
        super(c.getName(), scope, c);
    }

    @Override
    public RuntimeClassSymbol loadSymbols() {
        Class<?> c = (Class<?>) source;
        modifiers = c.getModifiers();
        for(Class<?> inner : c.getDeclaredClasses())
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

        return this;
    }
}