package gpuproj.srctree;

public class DoStatement extends LabelledStatement
{
    public Statement body;
    public Expression cond;

    public DoStatement(Scope scope, String label) {
        super(scope, label);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printLabel(sb);
        sb.append("do");
        printSub(sb, body);
        continueSub(sb);
        sb.append("while ").append(cond).append(';');
        return sb.toString();
    }
}
