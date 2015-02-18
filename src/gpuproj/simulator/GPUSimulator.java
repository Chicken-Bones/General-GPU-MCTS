package gpuproj.simulator;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.TreeNode;
import gpuproj.srctree.*;
import gpuproj.translator.CLProgramBuilder;
import gpuproj.translator.JavaTranslator;
import gpuproj.translator.KernelEnv;
import gpuproj.translator.TranslatedStruct;
import org.jocl.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.jocl.CL.*;

public abstract class GPUSimulator extends PlayoutSimulator
{
    private static KernelEnv env;

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

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, d.platform);

        env = new KernelEnv(d.id, contextProperties);
    }

    public static class BoardGameKernel
    {
        public final Class<? extends BoardGame> gameClass;
        public final int nodesPerKernel;
        public CLProgramBuilder program;
        public TranslatedStruct boardStruct;

        public cl_mem boardMem;
        public ByteBuffer boardBuffer;
        public cl_mem scoreMem;
        public int[] scoreBuffer;

        public BoardGameKernel(Class<? extends BoardGame> gameClass, int nodesPerKernel) {
            this.gameClass = gameClass;
            this.nodesPerKernel = nodesPerKernel;

            program = new CLProgramBuilder(env);
            JavaTranslator t = new JavaTranslator(program);
            ClassSymbol BoardGame = (ClassSymbol) TypeIndex.resolveType("gpuproj.game.BoardGame");
            ClassSymbol Game = (ClassSymbol) TypeIndex.resolveType(gameClass.getCanonicalName());
            ClassSymbol Board = (ClassSymbol) TypeRef.specify(BoardGame.typeParams.get(0), BoardGame.parameterPattern(), new TypeRef(Game));

            t.addStruct(Board);
            t.addGlobalInstance(Game);
            MethodSymbol playout = t.addGlobalMethod((MethodSymbol) TypeIndex.scope.resolve1("gpuproj.simulator.PlayoutSimulator.playout", Symbol.METHOD_SYM), Arrays.asList(Board, Game));
            t.translate();

            boardStruct = TranslatedStruct.translate(Board);

            boardBuffer = ByteBuffer.allocateDirect(nodesPerKernel * boardStruct.size);
            scoreBuffer = new int[nodesPerKernel];
            boardMem = clCreateBuffer(env.context, CL_MEM_READ_ONLY, boardBuffer.capacity(), null, null);
            scoreMem = clCreateBuffer(env.context, CL_MEM_READ_WRITE, scoreBuffer.length * Sizeof.cl_int, null, null);

            playout.params.get(0).type.pointer = 0;

            program.addKernelArg(new TypeRef(Board).point(1).modify(TypeRef.GLOBAL), "board").data = boardMem;
            program.addKernelArg(new TypeRef(PrimitiveSymbol.INT).point(1).modify(TypeRef.GLOBAL), "score").data = scoreMem;
            program.writeKernel("atom_add(score + get_group_id(0), "+t.kernelCall(playout, "board[get_group_id(0)]")+");");
            program.enableExtension("cl_khr_global_int32_base_atomics");
            program.build();
        }

        public void release() {
            program.release();
            clReleaseMemObject(boardMem);
            clReleaseMemObject(scoreMem);
        }

        public void run(List<TreeNode> nodes, int simsPerNode) {
            if (simsPerNode <= 0 || simsPerNode > env.maxWorkGroupSize)
                throw new IllegalArgumentException("Invald sims per node: " + simsPerNode+". Cap: "+env.maxWorkGroupSize);

            //execute kernel in batches of at most 128 nodes
            for (int group = 0; group < nodes.size(); group += nodesPerKernel) {
                int groupSize = Math.min(nodes.size()-group, nodesPerKernel);

                boardBuffer.rewind();
                for (int i = 0; i < groupSize; i++)
                    boardStruct.write(nodes.get(group+i).getBoard(), boardBuffer);

                boardBuffer.rewind();
                clEnqueueWriteBuffer(env.commandQueue, boardMem, true, 0, groupSize * boardStruct.size, Pointer.to(boardBuffer), 0, null, null);

                Arrays.fill(scoreBuffer, 0);
                clEnqueueWriteBuffer(env.commandQueue, scoreMem, true, 0, groupSize * Sizeof.cl_int, Pointer.to(scoreBuffer), 0, null, null);

                program.runKernel(new long[]{groupSize * simsPerNode}, new long[]{simsPerNode});
                clFinish(env.commandQueue);

                clEnqueueReadBuffer(env.commandQueue, scoreMem, true, 0, groupSize * Sizeof.cl_int, Pointer.to(scoreBuffer), 0, null, null);

                for (int i = 0; i < groupSize; i++)
                    nodes.get(group+i).update(BoardGame.floatScore(scoreBuffer[i], simsPerNode), simsPerNode);
            }
        }
    }

    private BoardGameKernel kernel;

    public GPUSimulator(Class<? extends BoardGame> gameClass) {
        kernel = new BoardGameKernel(gameClass, 128);
    }

    public int getMaxWorkGroupSize() {
        return env.maxWorkGroupSize;
    }

    public abstract int getWorkSize(int nodes);

    @Override
    public <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game) {
        if(kernel.gameClass != game.getClass())
            throw new IllegalArgumentException("Wrong game class "+game.getClass().getSimpleName()+" for "+kernel.gameClass.getSimpleName());

        int workSize = getWorkSize(nodes.size());
        kernel.run((List)nodes, workSize);

        simCount += nodes.size()*workSize;
        expCount++;
    }
}
