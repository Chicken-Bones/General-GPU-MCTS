package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.List;

/**
 * A symbol that can declare type params
 */
public interface ParameterisableSymbol extends Symbol, ScopeProvider
{
    public List<TypeParam> getTypeParams();
}
