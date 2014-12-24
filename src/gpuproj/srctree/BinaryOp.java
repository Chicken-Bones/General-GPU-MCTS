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
            case "*":
            case "/":
            case "%":
            case "+":
            case "-":
            case "&":
            case "^":
            case "|":
                return new TypeRef(SourceUtil.promoteNumeric(op1.returnType().type, op2.returnType().type));
            case ">>":
            case "<<":
            case ">>>":
                return new TypeRef(SourceUtil.promoteNumeric(op1.returnType().type, TypeIndex.INT));
            case "<":
            case "<=":
            case ">":
            case ">=":
            case "==":
            case "!=":
            case "&&":
            case "||":
                return new TypeRef(TypeIndex.INT);
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
    public String print() {
        return op1.print()+" "+op+" "+op2.print();
    }

    @Override
    public int precedence() {
        switch(op) {
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
