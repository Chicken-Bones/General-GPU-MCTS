package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class NewArrayExpression extends Expression
{
    public ArraySymbol type;
    /**
     * will be null for unspecified dimensions
     */
    public List<Expression> dimensions = new LinkedList<>();
    public InitialiserList init;

    @Override
    public TypeRef returnType() {
        return new TypeRef(type);
    }

    @Override
    public int precedence() {
        return 2;
    }

    public ConcreteTypeSymbol componentType() {
        TypeSymbol component = type;
        while(component instanceof ArraySymbol)
            component = ((ArraySymbol) component).componentType();

        return component.concrete();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(componentType().printName());
        for(Expression exp : dimensions) {
            sb.append('[');
            if(exp != null)
                sb.append(exp);
            sb.append(']');
        }

        if(init != null)
            sb.append(init);

        return sb.toString();
    }
}
