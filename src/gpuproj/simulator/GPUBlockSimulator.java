package gpuproj.simulator;

public class GPUBlockSimulator extends GPUSimulator
{
    public GPUBlockSimulator(int simsPerNode) {
        super(simsPerNode);
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
