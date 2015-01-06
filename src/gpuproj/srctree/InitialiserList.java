package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class InitialiserList extends Expression
{
    public TypeSymbol type;
    public List<Expression> elements = new LinkedList<Expression>();

    public InitialiserList(TypeSymbol type) {
        this.type = type;
    }

    @Override
    public TypeRef returnType() {
        return new TypeRef(type.array());
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public String toString() {
        return '{'+SourceUtil.listString(elements)+'}';
    }

    @Override
    public InitialiserList copy(Scope scope) {
        InitialiserList copy = new InitialiserList(type);
        for(Expression exp : elements)
            copy.elements.add(exp.copy(scope));

        return copy;
    }
}
