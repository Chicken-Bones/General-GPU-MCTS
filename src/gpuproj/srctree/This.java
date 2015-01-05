package gpuproj.srctree;

public class This extends Expression
{
    public ClassSymbol type;
    public This(ClassSymbol type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "this";
    }

    @Override
    public TypeRef returnType() {
        return new TypeRef(type);
    }

    @Override
    public int precedence() {
        return 1;
    }

    @Override
    public This copy(Scope scope) {
        return new This(type);
    }
}
