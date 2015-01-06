package gpuproj.srctree;

/**
 * Any 2 operand symbol expression, including assignment
 */
public class BinaryOp extends Expression
{
    public String op;
    public Expression op1;
    public Expression op2;

    public BinaryOp(String op, Expression op1, Expression op2) {
        this.op = op;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public TypeRef returnType() {
        if (op.equals("+") &&
                (op1.returnType().concrete() == TypeIndex.STRING || op2.returnType().concrete() == TypeIndex.STRING))
            return new TypeRef(TypeIndex.STRING);
        if (op.equals("*") ||
                op.equals("/") ||
                op.equals("%") ||
                op.equals("+") ||
                op.equals("-") ||
                op.equals("&") ||
                op.equals("^") ||
                op.equals("|"))
            return new TypeRef(PrimitiveSymbol.widen((PrimitiveSymbol) op1.returnType().type, (PrimitiveSymbol) op2.returnType().type));
        if (op.equals(">>") ||
                op.equals("<<") ||
                op.equals(">>>"))
            return new TypeRef(PrimitiveSymbol.widen(PrimitiveSymbol.INT, (PrimitiveSymbol) op1.returnType().type));
        if (op.equals("<") ||
                op.equals("<=") ||
                op.equals(">") ||
                op.equals(">=") ||
                op.equals("==") ||
                op.equals("!=") ||
                op.equals("&&") ||
                op.equals("||"))
            return new TypeRef(PrimitiveSymbol.INT);
        if (op.endsWith("="))
            return op1.returnType();

        throw new IllegalArgumentException("Unknown binary operator "+op);
    }

    @Override
    public String toString() {
        return op1+" "+op+" "+op2;
    }

    @Override
    public int precedence() {
        return precedence(op);
    }

    @Override
    public BinaryOp copy(Scope scope) {
        return new BinaryOp(op, op1.copy(scope), op2.copy(scope));
    }

    public static int precedence(String op) {
        if (op.equals("."))
            return 2;
        if (op.equals("*") ||
                op.equals("/") ||
                op.equals("%"))
            return 5;
        if (op.equals("+") ||
                op.equals("-"))
            return 6;
        if (op.equals(">>") ||
                op.equals("<<") ||
                op.equals(">>>"))
            return 7;
        if (op.equals("<") ||
                op.equals("<=") ||
                op.equals(">") ||
                op.equals(">="))
            return 8;
        if (op.equals("==") ||
                op.equals("!="))
            return 9;
        if (op.equals("&"))
            return 10;
        if (op.equals("^"))
            return 11;
        if (op.equals("|"))
            return 12;
        if (op.equals("&&"))
            return 13;
        if (op.equals("||"))
            return 14;
        if (op.endsWith("="))
            return 15;

        throw new IllegalArgumentException("Unknown binary operator "+op);
    }
}
