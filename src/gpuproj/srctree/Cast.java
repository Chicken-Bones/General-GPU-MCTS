package gpuproj.srctree;

public class Cast extends Expression
{
    public String type;
    public Expression exp;

    public Cast(String type, Expression exp) {
        this.type = type;
        this.exp = exp;
    }

    @Override
    public String returnType() {
        return type;
    }

    @Override
    public String print() {
        return "("+type+")"+exp.print();
    }

    @Override
    public int precedence() {
        return 3;
    }
}
