package gpuproj.translator;

import gpuproj.srctree.MethodSymbol;

public interface MethodRemapper
{
    public MethodSymbol map(MethodSymbol method);
}
