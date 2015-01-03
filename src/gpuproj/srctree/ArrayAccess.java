package gpuproj.srctree;

public class ArrayAccess extends Expression
{
    public Expression exp;
    public Expression index;

    public ArrayAccess(Expression exp, Expression index) {
        this.exp = exp;
        this.index = index;
    }

    @Override
    public TypeRef returnType() {
        return (exp.returnType().componentRef());
    }

    @Override
    public String toString() {
        return exp.toString()+'['+index+']';
    }

    @Override
    public int precedence() {
        return 2;
    }

    @Override
    public boolean lvalue() {
        return true;
    }
}
