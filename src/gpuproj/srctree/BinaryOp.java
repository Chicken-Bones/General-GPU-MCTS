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
        switch(op) {
            case "+":
                if(op1.returnType().concrete() == TypeIndex.STRING || op2.returnType().concrete() == TypeIndex.STRING)
                    return new TypeRef(TypeIndex.STRING);
            case "*":
            case "/":
            case "%":
            case "-":
            case "&":
            case "^":
            case "|":
                return new TypeRef(PrimitiveSymbol.widen((PrimitiveSymbol)op1.returnType().type, (PrimitiveSymbol)op2.returnType().type));
            case ">>":
            case "<<":
            case ">>>":
                return new TypeRef(PrimitiveSymbol.widen(PrimitiveSymbol.INT, (PrimitiveSymbol) op1.returnType().type));
            case "<":
            case "<=":
            case ">":
            case ">=":
            case "==":
            case "!=":
            case "&&":
            case "||":
                return new TypeRef(PrimitiveSymbol.INT);
            case "=":
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case ">>=":
            case "<<=":
            case ">>>=":
            case "&=":
            case "^=":
            case "|=":
                return op1.returnType();
        }
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

    public static int precedence(String op) {
        switch(op) {
            case ".":
                return 2;
            case "*":
            case "/":
            case "%":
                return 5;
            case "+":
            case "-":
                return 6;
            case ">>":
            case "<<":
            case ">>>":
                return 7;
            case "<":
            case "<=":
            case ">":
            case ">=":
                return 8;
            case "==":
            case "!=":
                return 9;
            case "&":
                return 10;
            case "^":
                return 11;
            case "|":
                return 12;
            case "&&":
                return 13;
            case "||":
                return 14;
            case "=":
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case ">>=":
            case "<<=":
            case ">>>=":
            case "&=":
            case "^=":
            case "|=":
                return 15;
        }
        throw new IllegalArgumentException("Unknown binary operator "+op);
    }
}
