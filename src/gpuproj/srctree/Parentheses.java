package gpuproj.srctree;

public class Parentheses extends Expression
{
    public Expression exp;

    public Parentheses(Expression exp) {
        this.exp = exp;
    }

    @Override
    public String returnType() {
        return exp.returnType();
    }

    @Override
    public String print() {
        return "("+exp.print()+")";
    }

    @Override
    public int precedence() {
        return 1;
    }
}
