package gpuproj.util;

import gpuproj.srctree.LocalSymbol;
import gpuproj.srctree.MethodSymbol;
import gpuproj.srctree.PrimitiveSymbol;
import gpuproj.srctree.TypeRef;
import gpuproj.translator.*;
import gpuproj.translator.CLSourceLoader.CLDecl;
import gpuproj.translator.CLSourceLoader.CLImpl;
import gpuproj.translator.CLProgramBuilder.KernelPreFunc;

import java.lang.reflect.Modifier;
import java.util.Random;

/**
 * Functions that will be linked to a specifically coded equivalent on the GPU but cannot be directly translated
 */
public class Portable implements CLStaticConverter
{
    private static Random rand = new Random();

    @CLStatic(Portable.class)
    public static int randInt(int max) {
        return rand.nextInt(max);
    }

    private MethodSymbol convertRandInt(MethodSymbol sym, JavaTranslator t) {
        //load the mwc64x_rng module
        new CLSourceLoader(t.program).load("mwc64x_rng.cl");

        //write a wrapper function
        t.program.declare(new CLDecl("int randInt(int max, mwc64x_state_t *rand);", "randInt"));
        t.program.implement(new CLImpl(
                "int randInt(int max, mwc64x_state_t *rand) {\n" +
                        "    return MWC64X_NextUint(rand) % max;\n" +
                        "}"));

        //create method symbol for java bindings
        sym = sym.copySig("randInt", t.scope(), this);
        sym.modifiers |= Modifier.STATIC;

        //add a kernel var for the rng
        t.getInfo(sym).kernelVars.add(t.getKernelVar("mwc64x_state_t", "rand"));

        //add a kernel argument for the rng base
        t.program.addKernelArg(new TypeRef(PrimitiveSymbol.LONG).modify(TypeRef.UNSIGNED), "randStart");

        //seed the rng, split the streams by a sufficently large number, a power of 2 because it improves the speed of the seed function
        t.program.writeKernel("MWC64X_SeedStreams(&rand, randStart, 1<<16);");

        //add the host callback to generate a new seed each run
        t.program.addPreFunc(new KernelPreFunc()
        {
            @Override
            public void prepareKernel(CLProgramBuilder program) {
                program.getKernelArg("randStart").data = rand.nextLong();
            }
        });

        return sym;
    }

    @CLStatic(Portable.class)
    public static long nthBit(long l, int n) {
        while(n-- > 0) l &= l-1;
        return l & -l;
    }

    private MethodSymbol convertNthBit(MethodSymbol sym, JavaTranslator t) {
        new CLSourceLoader(t.program).load("nth_bit.cl");

        //create method symbol for java bindings
        sym = sym.copySig("nthBit", t.scope(), this);
        sym.modifiers |= Modifier.STATIC;

        return sym;
    }

    public static long randBit(long l) {
        return nthBit(l, randInt(Long.bitCount(l)));
    }

    @Override
    public MethodSymbol convert(MethodSymbol sym, JavaTranslator t) {
        if(sym.getName().equals("randInt"))
            return convertRandInt(sym, t);
        else
            return convertNthBit(sym, t);
    }
}
