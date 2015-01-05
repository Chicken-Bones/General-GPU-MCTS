package gpuproj.srctree;

public class ContinueStatement extends Statement
{
    public LabelledStatement label;

    public ContinueStatement(LabelledStatement label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label == null ? "continue" : "continue "+label.label;
    }

    @Override
    public ContinueStatement copy(Scope scope) {
        return new ContinueStatement(label == null ? null : (LabelledStatement)scope.resolve1(label.label, Symbol.LABEL));
    }
}
