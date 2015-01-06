package gpuproj.srctree;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class UnaryOp extends Expression
{
    public static Set<String> symbols = new TreeSet<String>(Arrays.asList("++", "--", "+", "-", "~", "!"));

    public String op;
    public Expression operand;
    public boolean postfix;

    public UnaryOp(String op, Expression operand, boolean postfix) {
        this.op = op;
        this.operand = operand;
        this.postfix = postfix;
    }

    @Override
    public TypeRef returnType() {
        return new TypeRef(PrimitiveSymbol.widen(PrimitiveSymbol.INT, (PrimitiveSymbol) operand.returnType().type));
    }

    @Override
    public String toString() {
        return postfix ? operand+op : op+operand;
    }

    @Override
    public int precedence() {
        return 3;
    }

    @Override
    public UnaryOp copy(Scope scope) {
        return new UnaryOp(op, operand.copy(scope), postfix);
    }
}
