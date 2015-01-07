package gpuproj.simulator;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.TreeNode;
import gpuproj.srctree.*;
import gpuproj.translator.CLProgramBuilder;
import gpuproj.translator.JavaTranslator;
import gpuproj.translator.TranslatedStruct;
import org.jocl.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static org.jocl.CL.*;
import static org.jocl.CL.clCreateBuffer;

public class GPUSimulator extends PlayoutSimulator
{
    private static cl_device_id device;
    private static cl_context context;
    private static cl_command_queue commandQueue;

    static {
        initCL();
    }

    private static void initCL() {
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_GPU;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);
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
        checkWorkgroupSize();
    }

    private void checkWorkgroupSize() {
        int[] maxWorkGroupSize = new int[1];
        clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t, Pointer.to(maxWorkGroupSize), null);
        if(simsPerNode > maxWorkGroupSize[0])
            throw new IllegalArgumentException("Simulations per node ("+simsPerNode+") is more than the maximum work group size ("+maxWorkGroupSize[0]);
    }

    private void build(Class<? extends BoardGame> gameClass) {
        this.gameClass = gameClass;
        if(program != null)
            program.release();

        program = new CLProgramBuilder();
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
        program.writeKernel("atomic_add(score + get_group_id(0), "+t.kernelCall(playout, "board[get_group_id(0)]")+");");
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

    private void runKernel(List<TreeNode> nodes) {
        ensureCapacity(nodes.size());

        boardBuffer.rewind();
        for(TreeNode node : nodes)
            boardStruct.write(node.getBoard(), boardBuffer);

        boardBuffer.rewind();
        clEnqueueWriteBuffer(commandQueue, boardMem, true, 0, nodes.size() * boardStruct.size, Pointer.to(boardBuffer), 0, null, null);

        program.runKernel(commandQueue, new long[]{nodes.size()*simsPerNode}, new long[]{simsPerNode});

        clEnqueueReadBuffer(commandQueue, scoreMem, true, 0, nodes.size() * Sizeof.cl_int, Pointer.to(scoreBuffer), 0, null, null);

        int i = 0;
        for(TreeNode node : nodes)
            node.update(BoardGame.floatScore(scoreBuffer[i++], simsPerNode), simsPerNode);
    }

    @Override
    public <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game) {
        if(gameClass != game.getClass())
            build(game.getClass());

        runKernel((List)nodes);
        simCount += nodes.size() * simsPerNode;
    }

    @Override
    public String toString() {
        return "GPU "+simsPerNode+"/node";
    }
}
