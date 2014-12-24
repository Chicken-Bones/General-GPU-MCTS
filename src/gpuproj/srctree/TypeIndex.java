package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * Default scope
 */
public class TypeIndex implements ScopeProvider
{
    public static final PrimitiveSymbol BYTE = new PrimitiveSymbol("byte");
    public static final PrimitiveSymbol SHORT = new PrimitiveSymbol("short");
    public static final PrimitiveSymbol CHAR = new PrimitiveSymbol("char");
    public static final PrimitiveSymbol INT = new PrimitiveSymbol("int");
    public static final PrimitiveSymbol LONG = new PrimitiveSymbol("long");
    public static final PrimitiveSymbol FLOAT = new PrimitiveSymbol("float");
    public static final PrimitiveSymbol DOUBLE = new PrimitiveSymbol("double");
    public static final PrimitiveSymbol BOOLEAN = new PrimitiveSymbol("boolean");
    public static final PrimitiveSymbol VOID = new PrimitiveSymbol("void");

    public static TypeIndex instance;

    public static void newInstance() {
        instance = new TypeIndex();
    }

    public final Scope scope = new Scope(null, this);
    public List<SourceProvider> sourceProviders = new LinkedList<>();

    private TypeIndex() {
        scope.cache(BYTE);
        scope.cache(SHORT);
        scope.cache(CHAR);
        scope.cache(INT);
        scope.cache(LONG);
        scope.cache(FLOAT);
        scope.cache(DOUBLE);
        scope.cache(BOOLEAN);
        scope.cache(VOID);
    }

    private ReferenceSymbol findClass(String name) {
        String path = name.replace('.', '/')+".java";
        for(SourceProvider p : sourceProviders) {
            String source = p.findClass(path);
            if(source != null)
                return loadSourceFile(source);
        }

        try {
            return new RuntimeClassSymbol(scope, Class.forName(name, false, getClass().getClassLoader())).load();
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    private ReferenceSymbol loadSourceFile(String source) {
        SourceReader r = new SourceReader(source);
        SourceFile f = new SourceFile();
        while(!r.end()) {
            String stmt = r.readStatement();
            SourceReader sr = new SourceReader(stmt);
            switch(sr.readElement()) {
                case "package":
                    f.pkg = sr.substring(sr.seekCode());
                    break;
                case "import":
                    int mod = sr.readModifiers();
                    String imp = sr.substring(sr.seekCode());
                    f.addImport(imp, mod == Modifier.STATIC);
                    break;
                default:
                    return ClassSymbol.fromStatement(f.pkg, f.scope, stmt).load();
            }
        }
        return null;
    }

    @Override
    public Symbol resolveSingle(String name, int type) {
        if((type & Symbol.CLASS_SYM) != 0) {
            ReferenceSymbol sym = findClass(name);
            if(sym != null) return sym;
        }

        String parentName = SourceUtil.parentName(name);
        if(parentName.length() > 0) {
            ReferenceSymbol sym = (ReferenceSymbol) scope.resolve(parentName, Symbol.CLASS_SYM);
            if(sym != null) return sym.resolveSingle(SourceUtil.simpleName(name), type);
        }

        return null;
    }

    public TypeSymbol resolveType(String name) {
        return (TypeSymbol)scope.resolve(name, Symbol.TYPE_SYM);
    }

    public Symbol resolve(String name, int type) {
        return scope.resolve(name, type);
    }

    public void register(Symbol sym) {
        scope.cache.put(sym.fullname, sym);
    }
}
