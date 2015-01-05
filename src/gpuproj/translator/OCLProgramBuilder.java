package gpuproj.translator;

import gpuproj.srctree.TypeRef;
import org.jocl.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jocl.CL.*;

public class OCLProgramBuilder
{
    public static interface Declaration
    {
        public String getName();
        public String declare();
    }

    public static interface Implementation
    {
        public String implement();
    }

    public static class KernelArg
    {
        public final TypeRef type;
        public final String name;
        /**
         * Either a primitive type, or a cl_mem object
         */
        public Object data;

        private KernelArg(TypeRef type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public static interface GlobalFunc
    {
        public String getName();
        public String declare();
        public String implement();
    }

    private cl_kernel kernel;
    private cl_program program;
    private List<Declaration> declarations = new LinkedList<>();
    private Map<String, Declaration> declarationMap = new HashMap<>();
    private List<Implementation> implementations = new LinkedList<>();
    private List<KernelArg> args = new LinkedList<>();
    private Map<String, KernelArg> argMap = new HashMap<>();
    private StringBuilder kernelCode = new StringBuilder();

    public void declare(Declaration decl) {
        if(declarationMap.containsKey(decl.getName()))
            throw new IllegalArgumentException(decl.getName()+" already defined when declaring "+decl);

        declarations.add(decl);
        declarationMap.put(decl.getName(), decl);
    }

    public Declaration getDeclaration(String name) {
        return declarationMap.get(name);
    }

    public void implement(Implementation impl) {
        implementations.add(impl);
    }

    /**
     * Writes one or more lines to the kernel function
     * @param s One or more statements to write, should end with ;
     */
    public void writeKernel(String s) {
        kernelCode.append('\n').append(s);
    }

    public KernelArg addKernelArg(TypeRef type, String name) {
        if(argMap.containsKey(name))
            throw new IllegalArgumentException("Kernel argument "+name+" already added");

        KernelArg arg = new KernelArg(type, name);
        args.add(arg);
        argMap.put(name, arg);
        return arg;
    }

    public KernelArg getKernelArg(String name) {
        return argMap.get(name);
    }

    public void build() {
        //print declarations
        //write kernel
        //print implementations
        //build program
    }

    private void setKernelArg(int i) {
        Object value = args.get(i);
        int size;
        Pointer ptr;
        if(value instanceof cl_mem) {
            size = Sizeof.cl_mem;
            ptr = Pointer.to((cl_mem)value);
        } else if(value instanceof Double) {
            size = Sizeof.cl_double;
            ptr = Pointer.to(new double[]{(Double)value});
        } else if(value instanceof Float) {
            size = Sizeof.cl_float;
            ptr = Pointer.to(new float[]{(Float)value});
        } else if(value instanceof Long) {
            size = Sizeof.cl_long;
            ptr = Pointer.to(new long[]{(Long)value});
        } else if(value instanceof Integer) {
            size = Sizeof.cl_int;
            ptr = Pointer.to(new int[]{(Integer)value});
        } else if(value instanceof Short) {
            size = Sizeof.cl_short;
            ptr = Pointer.to(new short[]{(Short)value});
        } else if(value instanceof Byte) {
            size = Sizeof.cl_char;
            ptr = Pointer.to(new byte[]{(Byte)value});
        } else {
            throw new IllegalArgumentException(value + " is not a primitive or cl_mem");
        }

        clSetKernelArg(kernel, i, size, ptr);
    }

    public void runKernel(cl_command_queue cmdQueue, long[] global_work_size, long[] local_work_size) {
        for(int i = 0; i < args.size(); i++)
            setKernelArg(i);

        clEnqueueNDRangeKernel(cmdQueue, kernel, global_work_size.length, null, global_work_size, local_work_size, 0, null, null);
    }

    public void release() {

    }
}
