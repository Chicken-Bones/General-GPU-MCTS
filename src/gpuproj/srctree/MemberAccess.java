package gpuproj.srctree;

public class MemberAccess extends Expression
{
    Expression field;
    String member;

    @Override
    public String returnType() {
        return TypeIndex.instance.resolveClass(field.returnType()).resolveField(member).type;
    }

    @Override
    public String print() {
        return field.print() +
                (SourceUtil.pointerLevel(field.returnType()) == 0 ? "." : "->") +
                member;
    }

    @Override
    public int precedence() {
        return 2;
    }
}
