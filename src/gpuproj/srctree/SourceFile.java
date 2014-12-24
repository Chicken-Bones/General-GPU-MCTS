package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.LinkedList;
import java.util.List;

public class SourceFile implements ScopeProvider
{
    public static class Import
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
    public List<Import> imports = new LinkedList<>();

    @Override
    public Symbol resolveSingle(String name, int type) {
        for(Import imp : imports) {
            if((!imp.isStatic || (type & (Symbol.FIELD_SYM | Symbol.METHOD_SYM)) == 0) && (type & Symbol.CLASS_SYM) == 0)
                continue;

            if(imp.wildcard) {
                Symbol s = TypeIndex.instance.resolve(SourceUtil.combineName(imp.imp, name), type);
                if(s != null) return s;
            }
            else if(SourceUtil.simpleName(imp.imp).equals(name))
                return TypeIndex.instance.resolve(name, type);
        }
        return null;
    }

    public void addImport(String imp, boolean isStatic) {
        imports.add(new Import(imp, isStatic));
    }
}
