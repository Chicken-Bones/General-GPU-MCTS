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
    public String returnType() {
        if(value.endsWith("f"))
            return "float";
        if(value.contains(".") || value.contains("e") && !value.startsWith("0x"))
            return "double";
        if(value.endsWith("L"))
            return "long";
        return "int";
    }

    @Override
    public String print() {
        return value;
    }

    @Override
    public int precedence() {
        return 0;
    }
}
