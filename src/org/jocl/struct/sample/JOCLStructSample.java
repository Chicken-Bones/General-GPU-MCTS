/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2010 Marco Hutter - http://www.jocl.org/
 */

package org.jocl.struct.sample;

import static org.jocl.CL.*;

import java.nio.ByteBuffer;

import org.jocl.*;
import org.jocl.struct.*;
import org.jocl.struct.CLTypes.cl_float4;

/**
 * A sample class showing how to use the experimental struct 
 * support of JOCL
 */
public class JOCLStructSample
{
    
    /**
     * A class that is a Struct representing a particle
     * with mass, position and velocity 
     */
    public static class Particle extends Struct
    {
        public float mass;
        public cl_float4 position;
        public cl_float4 velocity;
        
        public String toString()
        {
            return "Particle[" +
            		"mass="+mass+"," +
            		"position="+position+"," +
            		"velocity="+velocity+"]";
        }
    }

    /**
     * The source code of the kernel
     */
    private static String programSource =
        
        // Definition of the Particle struct in the kernel
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    float mass;" + "\n" +
        "    float4 position;" + "\n" +
        "    float4 velocity;" + "\n" +
        "} Particle;"+ "\n" +

        // The actual kernel, performing some dummy computation
        "__kernel void test(__global Particle *particles)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    particles[gid].mass *= 2;"+ "\n" +
        "    particles[gid].position *= 2;"+ "\n" +
        "    particles[gid].velocity *= 2;"+ "\n" +
        "}";
    
    
    // CL state
    private static cl_context context;
    private static cl_command_queue commandQueue;
    private static cl_kernel kernel;
    private static cl_program program;
    
    /**
     * The entry point of this sample
     * 
     * @param args Not used
     */
    public static void main(String args[])
    {
        // Default OpenCL initialization
        defaultInitialization();


        // Initialization of an array containing some particles
        int n = 3;
        Particle particles[] = new Particle[n];
        for (int i=0; i<n; i++)
        {
            particles[i] = new Particle();
            particles[i].mass = i;
            particles[i].position.set(0, i);
            particles[i].position.set(1, i);
            particles[i].position.set(2, i);
            particles[i].position.set(3, i);
            particles[i].velocity.set(0, i);
            particles[i].velocity.set(1, i);
            particles[i].velocity.set(2, i);
            particles[i].velocity.set(3, i);
        }
        int structSize = SizeofStruct.sizeof(Particle.class);

        // Allocate a buffer that can store the particle data
        ByteBuffer particlesBuffer = Buffers.allocateBuffer(particles);
        
        // Write the particles into the buffer
        Buffers.writeToBuffer(particlesBuffer, particles);
        
        // Allocate the memory object for the particles that 
        // contains the data from the particle buffer
        cl_mem particlesMem = clCreateBuffer(context, 
            CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR,
            structSize * n, Pointer.to(particlesBuffer), null);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(particlesMem));
        
        // Set the work-item dimensions
        long global_work_size[] = new long[]{n};
        
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, null, 0, null, null);

        // Read back the data from to memory object to the particle buffer
        clEnqueueReadBuffer(commandQueue, particlesMem, true, 0, 
            structSize * n, Pointer.to(particlesBuffer), 0 , null, null);
        
        // Read the data from the particle buffer back into the particles
        particlesBuffer.rewind();
        Buffers.readFromBuffer(particlesBuffer, particles);

        // Print the result
        for (int i=0; i<n; i++)
        {
            System.out.println(i+": "+particles[i]);
        }
        
        // Clean up
        clReleaseMemObject(particlesMem);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }
    
    /**
     * Default initialization of the context, command queue, kernel
     * and program
     */
    private static void defaultInitialization()
    {
        // Obtain the platform IDs and initialize the context properties
        cl_platform_id platforms[] = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platforms[0]);
        
        // Create an OpenCL context on a GPU device
        context = clCreateContextFromType(
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
                return;
            }
        }

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
        
        // Get the list of GPU devices associated with the context
        long numBytes[] = new long[1];
        clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, null, numBytes); 
        
        // Obtain the cl_device_id for the first device
        int numDevices = (int) numBytes[0] / Sizeof.cl_device_id;
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetContextInfo(context, CL_CONTEXT_DEVICES, numBytes[0],  
            Pointer.to(devices), null);

        // Create a command-queue
        commandQueue = 
            clCreateCommandQueue(context, devices[0], 0, null);

        // Create the program from the source code
        program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        
        // Create the kernel
        kernel = clCreateKernel(program, "test", null);
    }

    
}
