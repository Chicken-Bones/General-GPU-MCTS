package gpuproj.srctree;

public class TernaryExpression extends Expression
{
    public Expression cond;
    public Expression then;
    public Expression otherwise;

    public TernaryExpression(Expression cond, Expression then, Expression otherwise) {
        this.cond = cond;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public TypeRef returnType() {
        TypeRef t1 = then.returnType();
        TypeRef t2 = otherwise.returnType();
        if(t1.type instanceof PrimitiveSymbol)
            return new TypeRef(PrimitiveSymbol.widen((PrimitiveSymbol)t1.type, (PrimitiveSymbol)t2.type));

        if(t1.type == PrimitiveSymbol.VOID)
            return t2;
        if(t2.type == PrimitiveSymbol.VOID)
            return t1;

        return t1;//shouldn't need to find common superclass
    }

    @Override
    public int precedence() {
        return 15;
    }

    @Override
    public String toString() {
        return cond + " ? " + then + " : "+otherwise;
    }
}
