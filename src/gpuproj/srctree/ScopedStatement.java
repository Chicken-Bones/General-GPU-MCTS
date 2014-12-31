package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.List;

public class ScopedStatement implements ScopeProvider
{
    public final Scope scope;

    public ScopedStatement(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {}
}
