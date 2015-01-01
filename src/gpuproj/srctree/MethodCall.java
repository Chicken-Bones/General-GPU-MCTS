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
        TypeRef t = method.returnType;
        if(!method.isStatic())
            t = t.specify(params.get(0).returnType());

        return t;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Expression> it = params.iterator();

        if(method.getName().equals("<init>")) {
            sb.append("new ").append(method.ownerName());
        } else {
            if (method.isStatic())
                sb.append(method.ownerName());
            else
                sb.append(it.next());
            sb.append('.').append(method.getName());
        }
        sb.append('(');

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
