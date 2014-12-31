package gpuproj.srctree;

public class BreakStatement extends Statement
{
    public LabelledStatement label;

    @Override
    public String toString() {
        return "continue"+(label == null ? "" : " "+label)+";";
    }
}
