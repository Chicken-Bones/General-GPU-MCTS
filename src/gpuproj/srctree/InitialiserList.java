package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class InitialiserList extends Expression
{
    public TypeSymbol type;
    public List<Expression> elements = new LinkedList<>();

    @Override
    public TypeRef returnType() {
        return null;
    }

    @Override
    public int precedence() {
        return 0;
    }
}
