/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import static org.jocl.CL.*;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import org.jocl.*;
import org.jocl.struct.*;
import org.jocl.struct.test.StructTestUtil.Function;

// "Structs are difficult, both alignment of individual components 
// and alignment and size of the overall struct must match on the 
// device and host code. 
// It probably will take trial and error to get it correct."
// Micah Villmow, Advanced Micro Devices Inc.
// (The information presented in this document is for informational 
// purposes only and may contain technical inaccuracies, omissions 
// and typographical errors. Links to third party sites are for 
// convenience only, and no endorsement is implied.)

/**
 * Tests for the experimental struct support
 */
public class JOCLStructTests
{
    /**
     * Whether detailed information should be printed
     */
    private static boolean printInfo = true;

    /**
     * Entry point of the tests
     * 
     * @param args Not used
     * @throws Exception If something goes wrong
     */
    public static void main(String args[]) throws Exception
    {
        test(StructSimple.class, 3, structSimpleKernelSource);
        test(StructVectorTypes.class, 3, structVectorTypesKernelSource);
        test(StructNested.class, 3, structNestedKernelSource);
        test(StructArrays.class, 3, structArraysKernelSource);
        test(StructVectorArrays.class, 3, structVectorArraysKernelSource);
        test(StructStructArrays.class, 3, structStructArraysKernelSource);
    }
    
    /**
     * Perform the test of the given struct class, using an array
     * of the given size, and the given kernel
     *  
     * @param structClass The struct class 
     * @param size The array size
     * @param kernelSource The source of the kernel
     * @throws Exception If something goes wrong
     */
    private static void test(
        Class<? extends Struct> structClass, int size, String kernelSource) 
        throws Exception
    {
        System.out.println("Testing "+structClass.getSimpleName());
        
        if (printInfo)
        {
            Struct.showLayout(structClass);
        }

        
        // Create the arrays of struct objects of the given class,
        // and apply to the i'th array element the function that
        // sets all accessors of the struct to 'i'
        Struct src[] = createArray(structClass, size);
        Struct dst[] = createArray(structClass, size);
        Struct ref[] = createArray(structClass, size);
        for (int i=0; i<size; i++)
        {
            final int value = i;
            Function setFunction = new Function()
            {
                @Override
                public Object apply(Object object)
                {
                    return value;
                }
            };
            StructTestUtil.applyToStruct(src[i], setFunction);
            StructTestUtil.applyToStruct(dst[i], setFunction);
            StructTestUtil.applyToStruct(ref[i], setFunction);
        }
        
        // Execute the computation with OpenCL
        test(src, dst, kernelSource);
        
        // Execute the computation on the reference. That is,
        // apply the function that multiplies values by 2 to
        // all accessors of the structs
        Function multiplyByTwo = new Function()
        {
            public Object apply(Object object)
            {
                Number number = (Number)object;
                return number.intValue() * 2;
            }
        };
        for (int i=0; i<size; i++)
        {
            StructTestUtil.applyToStruct(ref[i], multiplyByTwo);
        }
        
        // Print the result
        boolean passed = true;
        if (printInfo) System.out.println("Result: ");
        for (int i=0; i<size; i++)
        {
            if (printInfo)
            {
                System.out.println("src["+i+"] "+src[i]);
                System.out.println("dst["+i+"] "+dst[i]);
                System.out.println("ref["+i+"] "+ref[i]);
            }
            
            if (!Utils.deepEqual(dst[i], ref[i]))
            {
                passed = false;
                System.out.println("Not equal at index "+i);
            }
        }
        System.out.println(passed?"PASSED":"FAILED");
        System.out.println();
    }
    
    
    /**
     * Creates an array with the given size that is filled with objects of 
     * the given class
     * 
     * @param structClass The class of the elements
     * @param size The size of the array
     * @return The array
     * @throws Exception If something goes wrong
     */
    private static Struct[] createArray(Class<? extends Struct> structClass, int size) 
        throws Exception
    {
        Struct array[] = (Struct[])Array.newInstance(structClass, size);
        for (int i=0; i<size; i++)
        {
            array[i] = structClass.newInstance();
        }
        return array;
    }
    
    

    
    /**
     * The source code of a kernel that multiplies all elements of
     * a struct by 2
     */
    private static String structSimpleKernelSource =
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    short s;" + "\n" +
        "    float f;" + "\n" +
        "    char c;" + "\n" +
        "} StructSimple;"+ "\n" +
        ""+ "\n" +
        "__kernel void test(" + "\n" +
        "     __global StructSimple* srcStructs,"+ "\n" +
        "     __global StructSimple* dstStructs)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    dstStructs[gid].s = 2 * srcStructs[gid].s;"+ "\n" +
        "    dstStructs[gid].f = 2 * srcStructs[gid].f;"+ "\n" +
        "    dstStructs[gid].c = 2 * srcStructs[gid].c;"+ "\n" +
        "}";

    /**
     * The source code of a kernel that multiplies all elements of
     * a struct by 2
     */
    private static String structVectorTypesKernelSource =
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    char2 c2;" + "\n" +
        "    float4 f4;" + "\n" +
        "} StructVectorTypes;"+ "\n" +
        ""+ "\n" +
        "__kernel void test(" + "\n" +
        "     __global StructVectorTypes* srcStructs,"+ "\n" +
        "     __global StructVectorTypes* dstStructs)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    dstStructs[gid].c2 = (char)2 * srcStructs[gid].c2;"+ "\n" +
        "    dstStructs[gid].f4 = 2 * srcStructs[gid].f4;"+ "\n" +
        "}";
    
    /**
     * The source code of a kernel that multiplies all elements of
     * a struct by 2
     */
    private static String structNestedKernelSource =
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    short s;" + "\n" +
        "    float f;" + "\n" +
        "    char c;" + "\n" +
        "} StructSimple;"+ "\n" +
        "" + "\n" +
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    char2 c2;" + "\n" +
        "    float4 f4;" + "\n" +
        "} StructVectorTypes;"+ "\n" +
        ""+ "\n" +
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    short s;" + "\n" +
        "    StructSimple ss;" + "\n" +
        "    char c;" + "\n" +
        "    StructVectorTypes svt;" + "\n" +
        "} StructNested;"+ "\n" +
        ""+ "\n" +
        "__kernel void test(" + "\n" +
        "     __global StructNested* src,"+ "\n" +
        "     __global StructNested* dst)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    dst[gid].s = 2 * src[gid].s;"+ "\n" +
        "    dst[gid].ss.s = 2 * src[gid].ss.s;"+ "\n" +
        "    dst[gid].ss.f = 2 * src[gid].ss.f;"+ "\n" +
        "    dst[gid].ss.c = 2 * src[gid].ss.c;"+ "\n" +
        "    dst[gid].c = 2 * src[gid].c;"+ "\n" +
        "    dst[gid].svt.c2 = (char)2 * src[gid].svt.c2;"+ "\n" +
        "    dst[gid].svt.f4 = 2 * src[gid].svt.f4;"+ "\n" +
        "}";

    
    /**
     * The source code of a kernel that multiplies all elements of
     * a struct by 2
     */
    private static String structArraysKernelSource =
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    short as[3];" + "\n" +
        "    float af[3][3];" + "\n" +
        "} StructArrays;"+ "\n" +
        ""+ "\n" +
        "__kernel void test(" + "\n" +
        "     __global StructArrays* src,"+ "\n" +
        "     __global StructArrays* dst)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    for (int i=0; i<3; i++)"+ "\n" +
        "    {" + "\n" +
        "        dst[gid].as[i] = (short)2 * src[gid].as[i];"+ "\n" +
        "        for (int j=0; j<3; j++)"+ "\n" +
        "        {" + "\n" +
        "            dst[gid].af[i][j] = 2 * src[gid].af[i][j];"+ "\n" +
        "        }" + "\n" +
        "    }" + "\n" +
        "}";
    

    /**
     * The source code of a kernel that multiplies all elements of
     * a struct by 2
     */
    private static String structVectorArraysKernelSource =
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    short2 as2[3];" + "\n" +
        "    float4 af4[3][3];" + "\n" +
        "} StructVectorArrays;"+ "\n" +
        ""+ "\n" +
        "__kernel void test(" + "\n" +
        "     __global StructVectorArrays* src,"+ "\n" +
        "     __global StructVectorArrays* dst)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    for (int i=0; i<3; i++)"+ "\n" +
        "    {" + "\n" +
        "        dst[gid].as2[i] = (short)2 * src[gid].as2[i];"+ "\n" +
        "        for (int j=0; j<3; j++)"+ "\n" +
        "        {" + "\n" +
        "            dst[gid].af4[i][j] = 2 * src[gid].af4[i][j];"+ "\n" +
        "        }" + "\n" +
        "    }" + "\n" +
        "}";
    
    /**
     * The source code of a kernel that multiplies all elements of
     * a struct by 2
     */
    private static String structStructArraysKernelSource =
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    short s;" + "\n" +
        "    float f;" + "\n" +
        "    char c;" + "\n" +
        "} StructSimple;"+ "\n" +
        "" + "\n" +
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    char2 c2;" + "\n" +
        "    float4 f4;" + "\n" +
        "} StructVectorTypes;"+ "\n" +
        ""+ "\n" +
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    StructSimple ass[3];" + "\n" +
        "    char padding0[28];" + "\n" +
        "    StructVectorTypes asvt[3][3];" + "\n" +
        "    char padding1[32];" + "\n" +
        "} StructStructArrays;"+ "\n" +
        ""+ "\n" +
        "__kernel void test(" + "\n" +
        "     __global StructStructArrays* src,"+ "\n" +
        "     __global StructStructArrays* dst)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    for (int i=0; i<3; i++)"+ "\n" +
        "    {" + "\n" +
        "        dst[gid].ass[i].s = 2 * src[gid].ass[i].s;"+ "\n" +
        "        dst[gid].ass[i].f = 2 * src[gid].ass[i].f;"+ "\n" +
        "        dst[gid].ass[i].c = (char)2 * src[gid].ass[i].c;"+ "\n" +
        "        for (int j=0; j<3; j++)"+ "\n" +
        "        {" + "\n" +
        "            dst[gid].asvt[i][j].c2 = (char)2 * src[gid].asvt[i][j].c2;"+ "\n" +
        "            dst[gid].asvt[i][j].f4 = (char)2 * src[gid].asvt[i][j].f4;"+ "\n" +
        "        }" + "\n" +
        "    }" + "\n" +
        "}";
    
    

    
    public static void test(
        Struct[] srcStructs, Struct[] dstStructs, String kernelSource)
    {
        CL.setExceptionsEnabled(false);
        
        final boolean log = false;
        
        int structSize = SizeofStruct.sizeof(srcStructs[0].getClass());
        int n = srcStructs.length;
        
        // Obtain the platform IDs and initialize the context properties
        cl_platform_id platforms[] = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platforms[0]);
        
        // Create the OpenCL context on a GPU device
        if (log) System.out.println("Creating context...");
        cl_context context = clCreateContextFromType(
            contextProperties, CL_DEVICE_TYPE_GPU, null, null, null);
        if (context == null)
        {
            // If no context for a GPU device could be created,
            // try to create one for a CPU device.
            context = clCreateContextFromType(
                contextProperties, CL_DEVICE_TYPE_CPU, null, null, null);

            if (context == null)
            {
                System.out.println("Unable to create a context");
                System.exit(1);
            }
        }

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
        
        // Get the list of GPU devices associated with context
        if (log) System.out.println("Initializing device...");
        long numBytes[] = new long[1];
        clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, null, numBytes);
        int numDevices = (int) numBytes[0] / Sizeof.cl_device_id; 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetContextInfo(context, CL_CONTEXT_DEVICES, numBytes[0], 
            Pointer.to(devices), null);

        // Create a command-queue
        if (log) System.out.println("Creating command queue...");
        cl_command_queue commandQueue = 
            clCreateCommandQueue(context, devices[0],0, null);
        
        // Create the program
        if (log) System.out.println("Creating program...");
        cl_program program = clCreateProgramWithSource(context, 
            1, new String[]{ kernelSource }, null, null);
        
        // Build the program
        if (log) System.out.println("Building program...");
        clBuildProgram(program, 0, null, null, null, null);
        
        // Create the kernel
        if (log) System.out.println("Creating kernel...");
        cl_kernel kernel = clCreateKernel(program, "test", null);

        
        // Allocate the buffer memory objects
        if (log) System.out.println("Initializing buffers...");
        Pointer src = PointerStruct.to(srcStructs);
        
        cl_mem srcMem = clCreateBuffer(context, 
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            structSize * n, src, null);

        cl_mem dstMem = clCreateBuffer(context, 
            CL_MEM_READ_WRITE,
            structSize * n, null, null);
        
        // Set the arguments
        if (log) System.out.println("Setting arguments...");
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(srcMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(dstMem));
        
        // Set work-item dimensions and execute the kernel
        long globalWorkSize[] = new long[]{n};
        long localWorkSize[] = new long[]{1};

        if (log) System.out.println("Enqueueing kernel...");
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            globalWorkSize, localWorkSize, 0, null, null);
        
        // Read output image
        if (log) System.out.println("Enqueueing output read...");
        ByteBuffer dstBuffer = Buffers.allocateBuffer(dstStructs);
        Pointer dst = Pointer.to(dstBuffer);
        
        clEnqueueReadBuffer(commandQueue, dstMem, CL_TRUE, 0,
            structSize * n, dst, 0, null, null);

        // Read the contents of all structs from native memory
        Buffers.readFromBuffer(dstBuffer, dstStructs);
        
    }

    
}
