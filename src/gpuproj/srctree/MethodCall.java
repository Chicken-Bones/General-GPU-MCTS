package gpuproj.srctree;

import java.util.Iterator;
import java.util.List;

/**
 * Method and constructor (<init>) calls
 */
public class MethodCall extends Expression
{
    public MethodSymbol method;
    //note first param for virtual methods is the instance
    public List<Expression> params;

    public MethodCall(MethodSymbol method, List<Expression> params) {
        this.method = method;
        this.params = params;
    }

    @Override
    public TypeRef returnType() {
        return method.returnType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Expression> it = params.iterator();

        if(method.name.equals("<init>")) {
            sb.append("new ").append(method.owner.name);
        } else {
            if (method.isStatic())
                sb.append(method.owner.name);
            else
                sb.append(it.next());
            if (sb.length() > 0)
                sb.append('.');
        }
        sb.append(method.name).append('(');

        boolean first = true;
        while(it.hasNext()) {
            if(!first)
                sb.append(", ");

            sb.append(it.next());
            first = false;
        }

        sb.append(')');
        return sb.toString();
    }

    @Override
    public int precedence() {
        return 2;
    }
}
