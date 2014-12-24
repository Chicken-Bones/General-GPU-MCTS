package gpuproj.srctree;

public class Cast extends Expression
{
    public TypeRef type;
    public Expression exp;

    public Cast(TypeRef type, Expression exp) {
        this.type = type;
        this.exp = exp;
    }

    @Override
    public TypeRef returnType() {
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
