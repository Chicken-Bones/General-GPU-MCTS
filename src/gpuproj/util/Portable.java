package gpuproj.util;

import gpuproj.translator.OCLStatic;
import gpuproj.translator.OCLStaticConverter;

import java.util.Random;

/**
 * Functions that will be linked to a specifically coded equivalent on the GPU but cannot be directly translated
 */
public class Portable
{
    private static Random rand = new Random();

    @OCLStatic(OCLStaticConverter.class)
    public static int randInt(int max) {
        return rand.nextInt(max);
    }
}
