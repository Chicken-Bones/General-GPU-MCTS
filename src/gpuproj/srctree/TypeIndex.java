package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Default scope
 */
public class TypeIndex implements ScopeProvider
{
    public static TypeIndex instance;
    public final Scope scope = new Scope(null, this);
    public List<SourceProvider> sourceProviders = new LinkedList<>();

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
            //package, modifier, class, import, annotation

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
            Symbol parentSym = scope.resolve(parentName, Symbol.PACKAGE_SYM | Symbol.CLASS_SYM);
            if(parentSym instanceof PackageSymbol)
                return resolveClassOrPackage(name, type);
            return ((ClassSymbol)parentSym).resolveSingle(SourceUtil.simpleName(name), type);

        }
    }

    public ClassSymbol resolveClass(String name) {
        return (ClassSymbol)scope.resolve(name, Symbol.CLASS_SYM);
    }
}
