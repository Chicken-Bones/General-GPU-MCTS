package gpuproj.util;

import gpuproj.srctree.MethodSymbol;
import gpuproj.translator.JavaTranslator;
import gpuproj.translator.OCLStatic;
import gpuproj.translator.OCLStaticConverter;

import java.util.Random;

/**
 * Functions that will be linked to a specifically coded equivalent on the GPU but cannot be directly translated
 */
public class Portable implements OCLStaticConverter
{
    private static Random rand = new Random();

    @OCLStatic(Portable.class)
    public static int randInt(int max) {
        return rand.nextInt(max);
    }

    @Override
    public MethodSymbol convert(MethodSymbol sym, JavaTranslator translator) {
        return sym;
    }
}
