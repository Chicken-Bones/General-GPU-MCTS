package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * The default scope, and provider of all source files and class loading.
 * This scope provides classes and their members by global name
 */
public class TypeIndex implements ScopeProvider
{
    public static final TypeIndex instance = new TypeIndex();
    public static final Scope scope = new Scope(null, instance);
    public static List<SourceProvider> sourceProviders = new LinkedList<SourceProvider>();

    public static final RuntimeClassSymbol OBJECT = new RuntimeClassSymbol(scope, Object.class);
    public static final RuntimeClassSymbol STRING = new RuntimeClassSymbol(scope, String.class);
    public static final RuntimeClassSymbol CLASS = new RuntimeClassSymbol(scope, Class.class);

    static {
        for(PrimitiveSymbol p : PrimitiveSymbol.values)
            register(p);

        sourceProviders.add(new ClassPathSourceProvider("/src/"));
    }

    public static String provideSource(String path) {
        for(SourceProvider p : sourceProviders) {
            String source = p.provideSource(path);
            if(source != null)
                return source;
        }

        return null;
    }

    private ClassSymbol findClass(String name) {
        String source = provideSource(name.replace('.', '/') + ".java");
        if(source != null)
            return loadSourceFile(source);

        try {
            return new RuntimeClassSymbol(scope, Class.forName(name, false, getClass().getClassLoader())).load();
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    private ClassSymbol loadSourceFile(String source) {
        SourceReader r = new SourceReader(source);
        SourceFile f = new SourceFile();
        while(!r.end()) {
            String stmt = r.readStatement();
            SourceReader sr = new SourceReader(stmt);
            String s = sr.readElement();
            if (s.equals("package")) {
                f.pkg = sr.substring(sr.seekCode());
            } else if (s.equals("import")) {
                int mod = sr.readModifiers();
                String imp = sr.substring(sr.seekCode());
                f.addImport(imp, mod == Modifier.STATIC);
            } else
                return SourceClassSymbol.fromStatement(f.pkg, f.scope, stmt).load();
        }
        return null;
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if(type == Symbol.CLASS_SYM) {
            ClassSymbol sym = findClass(name);
            if(sym != null) {
                list.add(sym);
                return;
            }
        }

        String parentName = SourceUtil.parentName(name);
        if(parentName.length() > 0) {
            ClassSymbol sym = (ClassSymbol) scope.resolve1(parentName, Symbol.CLASS_SYM);
            if(sym != null) {
                sym.resolveOnce(SourceUtil.simpleName(name), type, list);
                return;
            }
        }

        if(type == Symbol.CLASS_SYM && !name.startsWith("java.lang")) {
            TypeSymbol sym = resolveType("java.lang." + name);
            if(sym != null)
                list.add(sym);
        }
    }

    @Override
    public Scope scope() {
        return scope;
    }

    /**
     * Resolves a class symbol by name in this scope
     */
    public static TypeSymbol resolveType(String name) {
        return (TypeSymbol) scope.resolve1(name, Symbol.CLASS_SYM);
    }

    /**
     * Caches a symbol under its global name
     */
    public static void register(Symbol sym) {
        scope.cache(sym, sym.globalName());
    }
}
