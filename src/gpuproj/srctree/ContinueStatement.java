package gpuproj.srctree;

public class ContinueStatement extends Statement
{
    public LabelledStatement label;

    @Override
    public String toString() {
        return "continue"+(label == null ? "" : " "+label)+";";
    }
}
