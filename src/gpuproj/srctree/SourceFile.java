package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Source file scope provider. Uses imports and package to combine names and delegate to TypeIndex
 */
public class SourceFile implements ScopeProvider
{
    private static class Import
    {
        public final String imp;
        public final boolean isStatic;
        public final boolean wildcard;

        public Import(String imp, boolean isStatic) {
            if (imp.endsWith("*")) {
                imp = imp.substring(0, imp.length() - 2);
                wildcard = true;
            } else
                wildcard = false;
            this.imp = imp;
            this.isStatic = isStatic;
        }

        @Override
        public String toString() {
            return "import " + (isStatic ? "static " : "") + imp + (wildcard ? ".*" : "");
        }
    }

    public final Scope scope = new Scope(this);
    public String pkg = "";
    public List<Import> imports = new LinkedList<Import>();

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        for(Import imp : imports) {
            if((!imp.isStatic || (type & (Symbol.FIELD_SYM | Symbol.METHOD_SYM)) == 0) && (type & Symbol.CLASS_SYM) == 0)
                continue;

            if(imp.wildcard)
                list.addAll(scope.parent.resolve(SourceUtil.combineName(imp.imp, name), type));
            else if(SourceUtil.simpleName(imp.imp).equals(name))
                list.addAll(scope.parent.resolve(imp.imp, type));

            if(!list.isEmpty())
                return;
        }

        if(type == Symbol.CLASS_SYM)//in package
            list.addAll(scope.parent.resolve(SourceUtil.combineName(pkg, name), type));
    }

    @Override
    public Scope scope() {
        return scope;
    }

    public void addImport(String imp, boolean isStatic) {
        imports.add(new Import(imp, isStatic));
    }
}
