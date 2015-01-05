package gpuproj.srctree;

public class WhileStatement extends LabelledStatement
{
    public Expression cond;
    public Statement body;

    public WhileStatement(Scope scope, String label) {
        super(scope, label);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printLabel(sb);
        sb.append("while").append(cond);
        printSub(sb, body);
        return sb.toString();
    }

    @Override
    public WhileStatement copy(Scope scope) {
        WhileStatement copy = new WhileStatement(scope, label);
        copy.cond = cond.copy(scope);
        copy.body = body.copy(copy.scope);
        return copy;
    }
}
