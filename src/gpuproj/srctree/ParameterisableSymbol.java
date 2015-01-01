package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.List;

public interface ParameterisableSymbol extends Symbol, ScopeProvider
{
    public List<TypeParam> getTypeParams();
}
