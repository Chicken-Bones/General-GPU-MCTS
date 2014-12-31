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
    public String toString() {
        return "("+type+")"+exp;
    }

    @Override
    public int precedence() {
        return 3;
    }
}
