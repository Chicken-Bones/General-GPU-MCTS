package gpuproj.srctree;

public class MemberAccess extends Expression
{
    Expression field;
    String member;

    @Override
    public TypeRef returnType() {
        return ((ReferenceSymbol)field.returnType().concrete()).getField(member).type;
    }

    @Override
    public String toString() {
        return field + "." + member;
    }

    @Override
    public int precedence() {
        return 2;
    }
}
