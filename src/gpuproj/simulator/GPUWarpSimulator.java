package gpuproj.simulator;

public class GPUWarpSimulator extends GPUSimulator
{
    public GPUWarpSimulator(int simsPerKernel) {
        super(simsPerKernel);
    }

    @Override
    public int getWorkSize(int nodes) {
        return Math.min(simsPerNode/nodes, getMaxWorkGroupSize());
    }

    @Override
    public String toString() {
        return "GPU "+simsPerNode+"/krnl";
    }
}
