package gpuproj.simulator;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.TreeNode;
import gpuproj.srctree.*;
import gpuproj.translator.CLProgramBuilder;
import gpuproj.translator.JavaTranslator;
import gpuproj.translator.TranslatedStruct;
import org.jocl.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.jocl.CL.*;
import static org.jocl.CL.clCreateBuffer;

public abstract class GPUSimulator extends PlayoutSimulator
{
    public static cl_device_id device;
    private static cl_context context;
    private static cl_command_queue commandQueue;
    private static int maxWorkGroupSize;

    static {
        initCL();
    }

    private static class Device
    {
        cl_platform_id platform;
        cl_device_id id;
        String name;
        int type;

        Device(cl_platform_id platform, cl_device_id id) {
            this.platform = platform;
            this.id = id;

            byte[] buf = new byte[100];
            clGetDeviceInfo(id, CL_DEVICE_NAME, buf.length, Pointer.to(buf), null);
            name = CLProgramBuilder.cString(buf);

            long[] lbuf = new long[1];
            clGetDeviceInfo(id, CL_DEVICE_TYPE, Sizeof.cl_long, Pointer.to(lbuf), null);
            type = (int) lbuf[0];
        }

        @Override
        public String toString() {
            String sType;
            switch(type) {
                case (int) CL_DEVICE_TYPE_CPU: sType = "CPU"; break;
                case (int) CL_DEVICE_TYPE_GPU: sType = "GPU"; break;
                case (int) CL_DEVICE_TYPE_ACCELERATOR: sType = "ACCEL"; break;
                case (int) CL_DEVICE_TYPE_CUSTOM: sType = "CUSTOM"; break;
                default: sType = "UNKNOWN"; break;
            }
            return sType+": "+name;
        }
    }

    private static Device selectDevice() {
        List<Device> deviceList = new LinkedList<Device>();

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        for(cl_platform_id platform : platforms) {
            // Obtain the number of devices for the platform
            int numDevicesArray[] = new int[1];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
            int numDevices = numDevicesArray[0];

            // Obtain a device ID
            cl_device_id devices[] = new cl_device_id[numDevices];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevices, devices, null);
            for(cl_device_id device : devices)
                deviceList.add(new Device(platform, device));
        }

        Properties p = new Properties();
        File file = new File("device.properties");
        try {
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                p.load(fin);
                fin.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String chosen = p.getProperty("device");
        if(chosen != null)
            for(Device device : deviceList)
                if(device.toString().equals(chosen))
                    return device;

        Device device = (Device) JOptionPane.showInputDialog(null, "Select GPU Device", "Device Selection", JOptionPane.QUESTION_MESSAGE, null, deviceList.toArray(), null);
        if(device == null)
            throw new RuntimeException("No Device Selected");

        p.setProperty("device", device.toString());
        try {
            if(!file.exists())
                file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            p.store(fout, "");
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return device;
    }

    private static void initCL() {

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        Device d = selectDevice();
        device = d.id;

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, d.platform);

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);

        int[] maxWorkGroupSizeArr = new int[1];
        clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t, Pointer.to(maxWorkGroupSizeArr), null);
        maxWorkGroupSize = maxWorkGroupSizeArr[0];
    }

    private Class<? extends BoardGame> gameClass;
    private CLProgramBuilder program;
    private TranslatedStruct boardStruct;

    private cl_mem boardMem;
    private ByteBuffer boardBuffer;
    private cl_mem scoreMem;
    private int[] scoreBuffer;

    public GPUSimulator(int simsPerNode) {
        super(simsPerNode);
    }

    private void build(Class<? extends BoardGame> gameClass) {
        this.gameClass = gameClass;
        if(program != null)
            program.release();

        program = new CLProgramBuilder(device);
        JavaTranslator t = new JavaTranslator(program);
        ClassSymbol BoardGame = (ClassSymbol) TypeIndex.resolveType("gpuproj.game.BoardGame");
        ClassSymbol Game = (ClassSymbol) TypeIndex.resolveType(gameClass.getCanonicalName());
        ClassSymbol Board = (ClassSymbol) TypeRef.specify(BoardGame.typeParams.get(0), BoardGame.parameterPattern(), new TypeRef(Game));

        t.addStruct(Board);
        t.addGlobalInstance(Game);
        MethodSymbol playout = t.addGlobalMethod((MethodSymbol) TypeIndex.scope.resolve1("gpuproj.simulator.PlayoutSimulator.playout", Symbol.METHOD_SYM), Arrays.asList(Board, Game));
        t.translate();

        playout.params.get(0).type.pointer = 0;

        program.addKernelArg(new TypeRef(Board).point(1).modify(TypeRef.GLOBAL), "board").data = boardMem;
        program.addKernelArg(new TypeRef(PrimitiveSymbol.INT).point(1).modify(TypeRef.GLOBAL), "score").data = scoreMem;
        program.writeKernel("atom_add(score + get_group_id(0), "+t.kernelCall(playout, "board[get_group_id(0)]")+");");
        program.enableExtension("cl_khr_global_int32_base_atomics");
        program.build(context);

        boardStruct = TranslatedStruct.translate(Board);
    }

    private void ensureCapacity(int nodeCount) {
        if(boardBuffer == null || nodeCount * boardStruct.size > boardBuffer.capacity()) {
            boardBuffer = ByteBuffer.allocateDirect(nodeCount * boardStruct.size);
            scoreBuffer = new int[nodeCount];
            if(boardMem != null) {
                clReleaseMemObject(boardMem);
                clReleaseMemObject(scoreMem);
            }
            program.getKernelArg("board").data = boardMem = clCreateBuffer(context, CL_MEM_READ_ONLY, boardBuffer.capacity(), null, null);
            program.getKernelArg("score").data = scoreMem = clCreateBuffer(context, CL_MEM_READ_WRITE, nodeCount * Sizeof.cl_int, null, null);
        }
    }

    public int getMaxWorkGroupSize() {
        return maxWorkGroupSize;
    }

    public abstract int getWorkSize(int nodes);

    private void runKernel(List<TreeNode> nodes) {
        ensureCapacity(nodes.size());

        boardBuffer.rewind();
        for(TreeNode node : nodes)
            boardStruct.write(node.getBoard(), boardBuffer);

        boardBuffer.rewind();
        clEnqueueWriteBuffer(commandQueue, boardMem, true, 0, nodes.size() * boardStruct.size, Pointer.to(boardBuffer), 0, null, null);

        Arrays.fill(scoreBuffer, 0);
        clEnqueueWriteBuffer(commandQueue, scoreMem, true, 0, nodes.size() * Sizeof.cl_int, Pointer.to(scoreBuffer), 0, null, null);

        int workSize = getWorkSize(nodes.size());
        program.runKernel(commandQueue, new long[]{nodes.size()*workSize}, new long[]{workSize});
        clFinish(commandQueue);

        clEnqueueReadBuffer(commandQueue, scoreMem, true, 0, nodes.size() * Sizeof.cl_int, Pointer.to(scoreBuffer), 0, null, null);

        int i = 0;
        for(TreeNode node : nodes)
            node.update(BoardGame.floatScore(scoreBuffer[i++], workSize), workSize);

        simCount += nodes.size()*workSize;
    }

    @Override
    public <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game) {
        if(gameClass != game.getClass())
            build(game.getClass());

        runKernel((List)nodes);
        expCount++;
    }
}
