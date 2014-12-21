package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.LinkedList;
import java.util.List;

public class SourceFile implements ScopeProvider
{
    public String pkg;
    public List<String> imports = new LinkedList<>();
    public List<String> wildcardImports = new LinkedList<>();
    public List<String> staticImports = new LinkedList<>();
    public List<String> staticWildcardImports = new LinkedList<>();

    @Override
    protected Symbol resolveSingle(String name) {
        for(String imp : imports) {
            String last = SourceUtil.simpleName(imp);
            if(last.equals("*")) {
                String pkg = imp.substring(0, imp.length()-2);
                if (TypeIndex.instance.getClassList(pkg).contains(name))
                    return TypeIndex.instance.resolve(pkg+"."+name);
            }
            else if(last.equals(name)) {
                return TypeIndex.instance.resolve(imp);
            }
        }

        for(String imp : staticImports) {
            String last = SourceUtil.simpleName(imp);
            String type = imp.substring(0, imp.length()-last.length()-1);
            if(last.equals("*")) {
                Object res = TypeIndex.instance.resolve(type).resolveInType(name);
                if(res != null) return res;
            }
            else if(last.equals(name)) {
                return TypeIndex.instance.resolve(type).resolveInType(name);
            }
        }

        return super.resolveFirst(name);
    }
}
