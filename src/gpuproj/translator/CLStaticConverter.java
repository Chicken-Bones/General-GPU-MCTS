package gpuproj.translator;

import gpuproj.srctree.MethodSymbol;

public interface CLStaticConverter
{
    /**
     * Convert a static method with an CLStatic annotation
     * May add extra parameters to method, which will be propogated back to kernel root
     */
    public MethodSymbol convert(MethodSymbol sym, JavaTranslator translator);
}
