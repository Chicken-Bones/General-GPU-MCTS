package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.LinkedList;
import java.util.List;

public class Block extends Statement implements ScopeProvider
{
    public final Scope scope;

    public List<Statement> statements = new LinkedList<>();
    private List<LocalSymbol> locals = new LinkedList<>();
    private List<LabelledStatement> labels = new LinkedList<>();

    public Block(Scope scope) {
        this.scope = new Scope(scope, this);
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if((type & Symbol.LOCAL_SYM) != 0) {
            for(LocalSymbol l : locals)
                if(l.name.equals(name))
                    list.add(l);
        }
        if((type & Symbol.LABEL) != 0) {
            for(LabelledStatement l : labels)
                if(l.label.equals(name))
                    list.add(l);
        }
    }

    /**
     * Scans the statement list for LocalSymbols and LabelSymbols and caches them for scope resolution
     */
    public void index() {
        labels.clear();
        locals.clear();

        for(Statement s : statements)
            if(s instanceof LocalSymbol)
                locals.add((LocalSymbol) s);
            else if(s instanceof CompactLocalDeclaration)
                locals.addAll(((CompactLocalDeclaration) s).locals);
            else if(s instanceof LabelledStatement && ((LabelledStatement) s).label != null)
                labels.add((LabelledStatement) s);
    }

    @Override
    public String toString() {
        if(statements.isEmpty())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(Statement stmt : statements) {
            sb.append('\n');
            printStatement(sb, stmt);
        }
        sb.append("\n}");

        return sb.toString();
    }

    protected void printStatement(StringBuilder sb, Statement stmt) {
        sb.append(indent(stmt.toString()));
        finishStatement(sb);
    }
}
