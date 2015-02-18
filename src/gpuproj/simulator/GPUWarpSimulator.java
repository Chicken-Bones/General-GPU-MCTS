package gpuproj.simulator;

import gpuproj.game.BoardGame;
import gpuproj.translator.KernelEnv;

public class GPUWarpSimulator extends GPUSimulator
{
    private int simsPerKernel;

    public GPUWarpSimulator(Class<? extends BoardGame> gameClass, int simsPerKernel) {
        super(gameClass);
        this.simsPerKernel = simsPerKernel;
    }

    @Override
    public int getWorkSize(int nodes) {
        return Math.min(Math.min(simsPerKernel, KernelEnv.maxWorkItems)/nodes, getMaxWorkGroupSize());
    }

    @Override
    public String toString() {
        return "GPU "+simsPerKernel+"/krnl";
    }
}
