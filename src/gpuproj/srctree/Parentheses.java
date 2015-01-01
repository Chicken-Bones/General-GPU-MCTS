package gpuproj.srctree;

public class Parentheses extends Expression
{
    public Expression exp;

    public Parentheses(Expression exp) {
        this.exp = exp;
    }

    @Override
    public TypeRef returnType() {
        return exp.returnType();
    }

    @Override
    public String toString() {
        return "("+exp+")";
    }

    @Override
    public int precedence() {
        return 1;
    }

    @Override
    public boolean lvalue() {
        return exp.lvalue();
    }
}
