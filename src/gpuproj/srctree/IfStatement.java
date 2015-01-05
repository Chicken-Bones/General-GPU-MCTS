package gpuproj.srctree;

public class IfStatement extends Statement
{
    public Expression cond;
    public Statement then;
    public Statement otherwise;

    public IfStatement(Expression cond, Statement then) {
        this.cond = cond;
        this.then = then;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("if ").append(cond);
        printSub(sb, then);
        if(otherwise != null) {
            continueSub(sb);
            sb.append("else");
            printSub(sb, otherwise);
        }
        return sb.toString();
    }

    @Override
    public IfStatement copy(Scope scope) {
        IfStatement copy = new IfStatement(cond.copy(scope), then.copy(scope));
        if(otherwise != null)
            copy.otherwise = otherwise.copy(scope);

        return copy;
    }
}
