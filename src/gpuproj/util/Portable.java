package gpuproj.util;

import gpuproj.srctree.MethodSymbol;
import gpuproj.srctree.PrimitiveSymbol;
import gpuproj.srctree.TypeRef;
import gpuproj.translator.*;
import gpuproj.translator.CLSourceLoader.CLDecl;
import gpuproj.translator.CLSourceLoader.CLImpl;
import gpuproj.translator.JavaTranslator.CLTypeSymbol;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import java.lang.reflect.Modifier;
import java.util.Random;

import static org.jocl.CL.*;

/**
 * Functions that will be linked to a specifically coded equivalent on the GPU but cannot be directly translated
 */
public class Portable implements CLStaticConverter
{
    private static ThreadLocal<Random> rand = new ThreadLocal<Random>();

    public static Random rand() {
        Random r = rand.get();
        if(r == null)
            rand.set(r = new Random());

        return r;
    }

    @CLStatic(Portable.class)
    public static int randInt(int max) {
        return rand().nextInt(max);
    }

    private static cl_mem randMem;
    private void initRandMem(KernelEnv env) {
        if(randMem != null)
            clReleaseMemObject(randMem);

        randMem = clCreateBuffer(env.context, CL_MEM_READ_WRITE, KernelEnv.maxWorkItems * Sizeof.cl_uint * 2, null, null);

        //run a kernel to seed the random
        CLProgramBuilder program = new CLProgramBuilder(env);
        new CLSourceLoader(program).load("mwc64x_rng.cl");
        program.addKernelArg(new TypeRef(PrimitiveSymbol.LONG).modify(TypeRef.UNSIGNED), "randStart").data = rand().nextLong();
        program.addKernelArg(new TypeRef(new CLTypeSymbol("mwc64x_state_t")).point(1).modify(TypeRef.GLOBAL), "randMem").data = randMem;

        program.writeKernel("mwc64x_state_t rand = randMem[get_global_id(0)];");
        program.writeKernel("MWC64X_SeedStreams(&rand, randStart, 1<<16);");
        program.writeKernel("randMem[get_global_id(0)] = rand;");
        program.build();
        program.runKernel(new long[]{KernelEnv.maxWorkItems}, new long[]{env.maxWorkGroupSize});
    }

    private MethodSymbol convertRandInt(MethodSymbol sym, JavaTranslator t) {
        initRandMem(t.program.env);

        //load the mwc64x_rng module
        new CLSourceLoader(t.program).load("mwc64x_step.cl");

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
        t.program.addKernelArg(new TypeRef(new CLTypeSymbol("mwc64x_state_t")).point(1).modify(TypeRef.GLOBAL), "randMem").data = randMem;

        //cache the rand instance
        t.program.writeKernel("rand = randMem[get_global_id(0)];");
        //load it back into global mem
        t.program.writePostKernel("randMem[get_global_id(0)] = rand;");

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
