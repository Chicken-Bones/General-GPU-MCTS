package gpuproj.translator;

import gpuproj.srctree.FieldSymbol;
import gpuproj.srctree.MethodSymbol;

public interface OCLGlobalConverter
{
    /**
     * Should change the type of sym, potentially defining a new struct and adding
     */
    public void convert(FieldSymbol sym, JavaTranslator translator);

    public MethodSymbol convert(MethodSymbol sym, JavaTranslator translator);
}
