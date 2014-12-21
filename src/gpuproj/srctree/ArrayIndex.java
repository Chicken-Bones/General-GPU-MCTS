package gpuproj.srctree;

public class ArrayIndex extends Expression
{
    public Expression index;
    public Expression array;

    @Override
    public String returnType() {
        return array.returnType();
    }

    @Override
    public String print() {
        return array.print()+"["+index.print()+"]";
    }

    @Override
    public int precedence() {
        return 2;
    }
}
