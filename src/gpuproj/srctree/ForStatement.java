package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class ForStatement extends LabelledStatement
{
    public List<Statement> init = new LinkedList<Statement>();
    public Expression cond;
    public List<Statement> update = new LinkedList<Statement>();
    public Statement body;

    public ForStatement(Scope scope, String label) {
        super(scope, label);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printLabel(sb);
        sb.append("for (");
        sb.append(SourceUtil.listString(init)).append("; ");
        sb.append(cond).append("; ");
        sb.append(SourceUtil.listString(update)).append(')');
        printSub(sb, body);
        return sb.toString();
    }

    @Override
    public ForStatement copy(Scope scope) {
        ForStatement copy = new ForStatement(scope, label);
        for(Statement stmt : init)
            copy.init.add(stmt.copy(scope));
        copy.cond = cond.copy(copy.scope);
        for(Statement stmt : update)
            copy.update.add(stmt.copy(copy.scope));
        copy.body = body.copy(copy.scope);
        return copy;
    }
}
