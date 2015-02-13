package gpuproj.translator;

import org.jocl.*;

import static org.jocl.CL.*;

public class KernelEnv
{
    /**
     * Sets a maximum buffer size to accomodate all work items.
     * If there are more nodes than maxWorkGroups, the kernel should be run again
     */
    public static final int maxWorkGroups = 128;

    public final cl_device_id device;
    public final cl_context context;
    public final cl_command_queue commandQueue;
    public final int maxWorkGroupSize;
    public final String vendor;

    public KernelEnv(cl_device_id device, cl_context_properties contextProperties) {
        this.device = device;

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);

        int[] maxWorkGroupSizeArr = new int[1];
        clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t, Pointer.to(maxWorkGroupSizeArr), null);
        maxWorkGroupSize = maxWorkGroupSizeArr[0];

        byte[] buf = new byte[100];
        clGetDeviceInfo(device, CL_DEVICE_VENDOR, buf.length, Pointer.to(buf), null);
        vendor = CLProgramBuilder.cString(buf);
    }

    public int maxWorkItems() {
        return maxWorkGroups * maxWorkGroupSize;
    }
}
