package gpuproj.translator;

import gpuproj.srctree.*;

public class Translator
{
    public static Object valueOf(Expression expr) {
        if(expr instanceof Literal)
            return valueOf((Literal) expr);
        if(expr instanceof BinaryOp)
            return valueOf((BinaryOp) expr);
        if(expr instanceof UnaryOp)
            return valueOf((UnaryOp) expr);
        if(expr instanceof Cast)
            return valueOf((Cast) expr);
        if(expr instanceof VariableAccess)
            return valueOf((VariableAccess) expr);

        throw new UnsupportedOperationException("Cannot calculate value of "+expr);
    }

    public static Object box(TypeSymbol type, Object value) {
        if(type == PrimitiveSymbol.DOUBLE)
            return ((Number)value).doubleValue();
        if(type == PrimitiveSymbol.FLOAT)
            return ((Number)value).floatValue();
        if(type == PrimitiveSymbol.LONG)
            return ((Number)value).longValue();
        if(type == PrimitiveSymbol.INT)
            return ((Number)value).intValue();
        if(type == PrimitiveSymbol.SHORT)
            return ((Number)value).shortValue();
        if(type == PrimitiveSymbol.BYTE)
            return ((Number)value).byteValue();
        if(type == PrimitiveSymbol.BOOLEAN)
            return (Boolean)value;

        throw new IllegalArgumentException("Cannot box "+value+" as "+type);
    }

    public static Object valueOf(UnaryOp expr) {
        Object value = valueOf(expr.operand);
        TypeSymbol type = expr.operand.returnType().type;

        try {
            if (type.isAssignableTo(PrimitiveSymbol.LONG))
                value = unaryOp(expr.op, ((Number) value).longValue());
            else if (type.isAssignableTo(PrimitiveSymbol.DOUBLE))
                value = unaryOp(expr.op, ((Number) value).doubleValue());
            else if (type == PrimitiveSymbol.BOOLEAN)
                value = unaryOp(expr.op, (Boolean) value);
            else
                throw new UnsupportedOperationException();

            return box(expr.returnType().type, value);
        } catch (UnsupportedOperationException ignored) {}

        throw new UnsupportedOperationException("Cannot compute literals for unary operator "+expr);
    }

    public static Object unaryOp(String op, long value) {
        switch(op) {
            case "+": return value;
            case "-": return -value;
            case "~": return ~value;
        }

        throw new UnsupportedOperationException();
    }

    public static Object unaryOp(String op, double value) {
        switch(op) {
            case "+": return value;
            case "-": return -value;
        }

        throw new UnsupportedOperationException();
    }

    public static Object unaryOp(String op, boolean value) {
        switch(op) {
            case "!": return !value;
        }

        throw new UnsupportedOperationException();
    }

    public static Object valueOf(BinaryOp expr) {
        Object op1 = valueOf(expr.op1);
        Object op2 = valueOf(expr.op2);
        TypeSymbol type1 = expr.op1.returnType().type;
        TypeSymbol type2 = expr.op2.returnType().type;

        try {
            Object value;
            if (type1.isAssignableTo(PrimitiveSymbol.LONG) && type2.isAssignableTo(PrimitiveSymbol.LONG))
                value = binaryOp(expr.op, ((Number) op1).longValue(), ((Number) op2).longValue());
            else if (type1.isAssignableTo(PrimitiveSymbol.DOUBLE) && type2.isAssignableTo(PrimitiveSymbol.DOUBLE))
                value = binaryOp(expr.op, ((Number) op1).doubleValue(), ((Number) op2).doubleValue());
            else if (type1 == PrimitiveSymbol.BOOLEAN && type2 == PrimitiveSymbol.BOOLEAN)
                value = binaryOp(expr.op, (Boolean) op1, (Boolean) op2);
            else
                throw new UnsupportedOperationException();

            return box(expr.returnType().type, value);
        } catch (UnsupportedOperationException ignored) {}

        throw new UnsupportedOperationException("Cannot compute literals for binary operator "+expr.op+" "+expr);
    }

    public static Object binaryOp(String op, long a, long b) {
        switch(op) {
            case "+": return a + b;
            case "*": return a * b;
            case "/": return a / b;
            case "%": return a % b;
            case "-": return a - b;
            case "&": return a & b;
            case "^": return a ^ b;
            case "|": return a | b;
            case ">>": return a << b;
            case "<<": return a << b;
            case ">>>": return a >>> b;
            case "<": return a < b;
            case "<=": return a <= b;
            case ">": return a > b;
            case ">=": return a >= b;
            case "==": return a == b;
            case "!=": return a != b;
        }

        throw new UnsupportedOperationException();
    }

    public static double binaryOp(String op, double a, double b) {
        switch(op) {
            case "+": return a + b;
            case "*": return a * b;
            case "/": return a / b;
            case "%": return a % b;
            case "-": return a - b;
        }

        throw new UnsupportedOperationException();
    }

    public static Object binaryOp(String op, boolean a, boolean b) {
        switch(op) {
            case "==": return a == b;
            case "!=": return a != b;
            case "&&": return a && b;
            case "||": return a || b;
        }

        throw new UnsupportedOperationException();
    }

    public static Object valueOf(Literal expr) {
        TypeRef type = expr.returnType();
        if(type.type == TypeIndex.instance().OBJECT)
            return null;
        if(type.type == TypeIndex.instance().CLASS)
            return type.params.get(0).classType().runtimeClass();
        if(type.type == PrimitiveSymbol.BOOLEAN)
            return Boolean.valueOf(expr.value);
        if(type.type == PrimitiveSymbol.CHAR)
            throw new UnsupportedOperationException("Char literal evaluation");
        if(type.type == TypeIndex.instance().STRING)
            throw new UnsupportedOperationException("String literal evaluation");
        if(type.type == PrimitiveSymbol.FLOAT)
            return Float.valueOf(expr.value);
        if(type.type == PrimitiveSymbol.DOUBLE)
            return Double.valueOf(expr.value);
        if(type.type == PrimitiveSymbol.LONG)
            return Long.valueOf(expr.value);
        if(type.type == PrimitiveSymbol.INT)
            return Integer.valueOf(expr.value);

        throw new IllegalArgumentException("Strange literal "+expr);
    }

    private static void readInitialiserDimensions(int depth, int[] dimensions, Expression exp) {
        if(!(exp instanceof InitialiserList))
            throw new IllegalArgumentException("Multidimensional array does not use a nested initialiser list");

        InitialiserList init = (InitialiserList)exp;
        int dim = init.elements.size();
        if(dim == 0)
            throw new IllegalArgumentException("Arrays must have non-zero dimensions");
        if(dimensions[depth] == 0)
            dimensions[depth] = dim;
        else if(dim != dimensions[depth])
            throw new IllegalArgumentException("Multidimensional arrays must be square");

        if(depth + 1 < dimensions.length)
            for(Expression exp2 : init.elements)
                readInitialiserDimensions(depth+1, dimensions, exp2);
    }

    public static int[] getArrayDimensions(Variable var) {
        Expression init = var.initialiser();
        if(!(init instanceof NewArray))
            throw new IllegalArgumentException("Cannot determine array dimensions from field initialiser. "+var.getType()+" "+var.getName()+" = "+init);

        NewArray newExpr = (NewArray)init;
        int[] dimensions = new int[newExpr.dimensions.size()];
        if(newExpr.init != null) {
            readInitialiserDimensions(0, dimensions, newExpr.init);
            return dimensions;
        }

        for(int i = 0; i < dimensions.length; i++) {
            Expression expr = newExpr.dimensions.get(0);
            try {
                dimensions[i] = ((Number)valueOf(expr)).intValue();
            } catch (Exception e) {
                throw new IllegalArgumentException("Could evaluate array dimension expression "+expr);
            }
        }
        return dimensions;
    }
}
