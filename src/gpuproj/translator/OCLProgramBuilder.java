package gpuproj.translator;

import gpuproj.StatDialog;
import gpuproj.srctree.SourceReader;
import gpuproj.srctree.SourceUtil;
import gpuproj.srctree.Statement;
import gpuproj.srctree.TypeRef;
import org.jocl.*;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jocl.CL.*;

public class OCLProgramBuilder
{
    public static class Define
    {
        public final String line;

        public Define(String line) {
            if(!line.startsWith("#define "))
                throw new IllegalArgumentException("must start with #define :"+line);

            this.line = line;
        }

        public String identifier() {
            return new SourceReader(line.substring(8)).readElement();
        }

        public String value() {
            SourceReader r = new SourceReader(line.substring(8));
            r.readElement();//identifier
            r.seekCode();
            return (r.end()) ? "" : r.substring(r.pos);
        }
    }

    public static interface Declaration
    {
        public List<String> identifiers();
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

        @Override
        public String toString() {
            TypeRef.printCL = true;
            String s = type+" "+name;
            TypeRef.printCL = false;
            return s;
        }
    }

    public interface KernelPreFunc
    {
        public void prepareKernel(OCLProgramBuilder program);
    }

    private cl_kernel kernel;
    private cl_program program;

    private List<Define> definitions = new LinkedList<Define>();
    private Map<String, Define> defineMap = new HashMap<String, Define>();
    private List<Declaration> declarations = new LinkedList<Declaration>();
    private Map<String, Declaration> declarationMap = new HashMap<String, Declaration>();
    private List<KernelArg> args = new LinkedList<KernelArg>();
    private Map<String, KernelArg> argMap = new HashMap<String, KernelArg>();
    private List<Implementation> implementations = new LinkedList<Implementation>();
    private StringBuilder kernelCode = new StringBuilder();
    private List<KernelPreFunc> kernelPreFuncs = new LinkedList<KernelPreFunc>();

    public void define(String s) {
        Define def = new Define(s);
        String identifier = def.identifier();
        if(defineMap.containsKey(identifier))
            throw new IllegalArgumentException(identifier+" already defined when defining "+def);

        defineMap.put(identifier, def);
        definitions.add(def);
    }

    public void undef(String identifier) {
        Define def = defineMap.get(identifier);
        if(def != null)
            definitions.remove(def);
    }

    public String getDef(String identifier) {
        Define d = defineMap.get(identifier);
        return d == null ? null : d.value();
    }

    public void declare(Declaration decl) {
        for(String identifier : decl.identifiers())
            if(declarationMap.containsKey(identifier))
                throw new IllegalArgumentException(identifier+" already defined when declaring "+decl);
            else
                declarationMap.put(identifier, decl);

        declarations.add(decl);
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
        kernelCode.append('\n').append(Statement.indent(s));
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

    public void addPreFunc(KernelPreFunc func) {
        kernelPreFuncs.add(func);
    }

    public void build(cl_context context) {
        if(program != null)
            throw new IllegalStateException("Program already built");

        StringBuilder sb = new StringBuilder();
        for(Define define : definitions)
            sb.append(define.line).append("\n");

        for(Declaration decl : declarations)
            sb.append(decl.declare()).append("\n\n");

        for(Implementation impl : implementations)
            sb.append(impl.implement()).append("\n\n");

        sb.append("__kernel void kernel_main(").append(SourceUtil.listString(args)).append(") {");
        sb.append(kernelCode);
        sb.append("\n}");

        String source = sb.toString();
        logSource(source);

        program = clCreateProgramWithSource(context, 1, new String[]{source}, null, null);
        clBuildProgram(program, 0, null, "", null, null);
        kernel = clCreateKernel(program, "kernel_main", null);
    }

    private static int programID = 0;
    private static void logSource(String source) {
        try {
            File file = new File(StatDialog.getLogDir(), "program" + programID++ + ".cl");
            file.createNewFile();
            FileWriter w = new FileWriter(file);
            w.append(source);
            w.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setKernelArg(int i) {
        Object value = args.get(i).data;
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
        for(KernelPreFunc func : kernelPreFuncs)
            func.prepareKernel(this);

        for(int i = 0; i < args.size(); i++)
            setKernelArg(i);

        clEnqueueNDRangeKernel(cmdQueue, kernel, global_work_size.length, null, global_work_size, local_work_size, 0, null, null);
    }

    public void release() {
        clReleaseKernel(kernel);
        clReleaseProgram(program);
    }
}
