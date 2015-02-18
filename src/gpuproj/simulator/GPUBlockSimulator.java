package gpuproj.simulator;

import gpuproj.game.BoardGame;

public class GPUBlockSimulator extends GPUSimulator
{
    private int simsPerNode;

    public GPUBlockSimulator(Class<? extends BoardGame> gameClass, int simsPerNode) {
        super(gameClass);
        this.simsPerNode = simsPerNode;
    }

    @Override
    public int getWorkSize(int nodes) {
        return simsPerNode;
    }

    @Override
    public String toString() {
        return "GPU "+simsPerNode+"/node";
    }
}
