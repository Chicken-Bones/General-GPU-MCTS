package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.util.List;

public abstract class LabelledStatement extends Statement implements Symbol, ScopeProvider
{
    public final Scope scope;
    public String label;

    public LabelledStatement(Scope scope, String label) {
        this.scope = new Scope(scope, this);
        this.label = label;
    }

    @Override
    public int symbolType() {
        return Symbol.LABEL;
    }

    @Override
    public String globalName() {
        return null;
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if(type == Symbol.LABEL && label != null && label.equals(name))
            list.add(this);
    }

    @Override
    public Scope scope() {
        return scope;
    }

    public void printLabel(StringBuilder sb) {
        if(label != null)
            sb.append(label).append(": ");
    }
}
