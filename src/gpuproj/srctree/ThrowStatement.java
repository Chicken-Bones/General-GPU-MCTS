package gpuproj.srctree;

public class ThrowStatement extends Statement
{
    public Expression exp;

    public ThrowStatement(Expression exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        return "throw "+exp;
    }
}
