package gpuproj.srctree;

import java.lang.reflect.Modifier;

/**
 * Method and constructor (<init>) calls
 */
public class MethodCall extends Expression
{
    public MethodSymbol method;
    public Expression[] params;

    @Override
    public TypeRef returnType() {
        return method.returnType;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        int p = 0;
        if(method.name.equals("<init>")) {
            sb.append("new ").append(method.owner.name);
        } else {
            if (Modifier.isStatic(method.modifiers))
                sb.append(method.owner.name);
            else
                sb.append(params[p++].print());
            if (sb.length() > 0)
                sb.append('.');
        }
        sb.append(method.name).append('(');

        while(p < params.length) {
            if(sb.charAt(sb.length()-1) != '(')
                sb.append(", ");

            Expression exp = params[p];
            /*if(SourceUtil.pointerLevel(method.params.get()paramTypes[p]) > SourceUtil.pointerLevel(exp.returnType())) {
                sb.append('&');//take address for passing to methods requiring pointer args
                if(exp.precedence() <= 3)//address of precedence
                    exp = new Parentheses(exp);
            }*/
            sb.append(exp.print());
        }

        sb.append(')');
        return sb.toString();
    }

    @Override
    public int precedence() {
        return 2;
    }
}
