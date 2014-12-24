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

    private boolean isPackage(String path) {
        for(SourceProvider p : sourceProviders)
            if(p.packageExists(path))
                return true;

        return false;
    }

    private ClassSymbol findClass(String path) {
        for(SourceProvider p : sourceProviders) {
            String source = p.findClass(path);
            if(source != null)
                return loadSourceFile(source);
        }

        return null;
    }

    private ClassSymbol loadSourceFile(String source) {
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
                    return ClassSymbol.fromStatement(f.scope, f.pkg, stmt);
            }
        }
        return null;
    }

    private Symbol resolveClassOrPackage(String name, int type) {
        String path = name.replace('.', '/');
        if((type & Symbol.PACKAGE_SYM) != 0 && isPackage(path))
            return new PackageSymbol(name);
        if((type & Symbol.CLASS_SYM) != 0)
            return findClass(path + ".java");
        return null;
    }

    @Override
    public Symbol resolveSingle(String name, int type) {
        String parentName = SourceUtil.parentName(name);
        if(parentName.length() == 0) {
            return resolveClassOrPackage(name, type);
        } else {
            Symbol sym = scope.resolve(parentName, Symbol.PACKAGE_SYM | Symbol.CLASS_SYM);
            if(sym == null)
                new Object();
            if(sym instanceof PackageSymbol)
                return resolveClassOrPackage(name, type);
            return ((ClassSymbol)sym).resolveSingle(SourceUtil.simpleName(name), type);
        }
    }

    public TypeSymbol resolveType(String name) {
        return (TypeSymbol)scope.resolve(name, Symbol.TYPE_SYM);
    }

    public Symbol resolve(String name, int type) {
        return scope.resolve(name, type);
    }

    public void register(ClassSymbol sym) {
        scope.cache.put(sym.fullname, sym);
    }
}
