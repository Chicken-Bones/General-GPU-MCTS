package gpuproj.srctree;

public class ReturnStatement extends Statement
{
    public Expression exp;

    public ReturnStatement(Expression exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        return "return "+exp;
    }
}
