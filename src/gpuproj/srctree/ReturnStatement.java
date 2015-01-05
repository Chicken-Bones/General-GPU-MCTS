package gpuproj.srctree;

public class ReturnStatement extends Statement
{
    public Expression exp;

    public ReturnStatement(Expression exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        return exp == null ? "return" : "return "+exp;
    }

    @Override
    public ReturnStatement copy(Scope scope) {
        return new ReturnStatement(exp == null ? null : exp.copy(scope));
    }
}
