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
    private static TypeIndex instance;

    public static TypeIndex instance() {
        return instance;
    }

    public static void newInstance() {
        new TypeIndex();
    }

    public final Scope scope = new Scope(null, this);
    public final RuntimeClassSymbol OBJECT;
    public List<SourceProvider> sourceProviders = new LinkedList<>();

    private TypeIndex() {
        instance = this;
        OBJECT = new RuntimeClassSymbol(scope, Object.class);
        for(PrimitiveSymbol p : PrimitiveSymbol.values)
            register(p);
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
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if((type & Symbol.CLASS_SYM) != 0) {
            ReferenceSymbol sym = findClass(name);
            if(sym != null) {
                list.add(sym);
                return;
            }
        }

        String parentName = SourceUtil.parentName(name);
        if(parentName.length() > 0) {
            ReferenceSymbol sym = (ReferenceSymbol) scope.resolve1(parentName, Symbol.CLASS_SYM);
            if(sym != null) {
                sym.resolveOnce(SourceUtil.simpleName(name), type, list);
                return;
            }
        }

        if(!name.startsWith("java.lang"))
            resolveOnce("java.lang."+name, type, list);
    }

    public ConcreteTypeSymbol resolveType(String name) {
        return (ConcreteTypeSymbol)scope.resolve1(name, Symbol.CLASS_SYM);
    }

    public List<Symbol> resolve(String name, int type) {
        return scope.resolve(name, type);
    }

    public void register(GlobalSymbol sym) {
        scope.cache(sym, sym.fullname);
    }
}
