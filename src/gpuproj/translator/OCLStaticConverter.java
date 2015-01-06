package gpuproj.translator;

import gpuproj.srctree.MethodSymbol;

public interface OCLStaticConverter
{
    /**
     * Convert a static method with an OCLStatic annotation
     * May add extra parameters to method, which will be propogated back to kernel root
     */
    public MethodSymbol convert(MethodSymbol sym, JavaTranslator translator);
}
