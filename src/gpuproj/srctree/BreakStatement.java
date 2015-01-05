package gpuproj.srctree;

public class BreakStatement extends Statement
{
    public LabelledStatement label;

    public BreakStatement(LabelledStatement label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label == null ? "break" : "break "+label.label;
    }

    @Override
    public BreakStatement copy(Scope scope) {
        return new BreakStatement(label == null ? null : (LabelledStatement)scope.resolve1(label.label, Symbol.LABEL));
    }
}
