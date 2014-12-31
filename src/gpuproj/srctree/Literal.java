package gpuproj.srctree;

public class Literal extends Expression
{
    String value;

    public Literal(String value) {
        if(value.endsWith("D") || value.endsWith("d")) {
            value = value.substring(0, value.length()-1);
            if(!value.contains("."))
                value += ".0";
        }
        this.value = value;
    }

    @Override
    public TypeRef returnType() {
        if(value.equals("null"))
            return new TypeRef(TypeIndex.instance().OBJECT);
        if(value.equals("true") || value.equals("false"))
            return new TypeRef(PrimitiveSymbol.INT);
        if(value.startsWith("'"))
            return new TypeRef(PrimitiveSymbol.CHAR);
        if(value.startsWith("\""))
            return new TypeRef(TypeIndex.instance().STRING);
        if(value.endsWith("f"))
            return new TypeRef(PrimitiveSymbol.FLOAT);
        if(value.contains(".") || value.contains("e") && !value.startsWith("0x"))
            return new TypeRef(PrimitiveSymbol.DOUBLE);
        if(value.endsWith("L"))
            return new TypeRef(PrimitiveSymbol.LONG);
        return new TypeRef(PrimitiveSymbol.INT);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int precedence() {
        return 0;
    }
}
