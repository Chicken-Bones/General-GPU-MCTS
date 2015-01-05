package gpuproj.srctree;

public class EmptyStatement extends Statement
{
    @Override
    public String toString() {
        return "";
    }

    @Override
    public EmptyStatement copy(Scope scope) {
        return new EmptyStatement();
    }
}
