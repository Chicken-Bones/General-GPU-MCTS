package gpuproj.srctree;

import java.util.*;

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
        Map<TypeParam, TypeSymbol> typeMap = TypeRef.specify(t.getParams(), method.params, params);
        return t.mapParams(typeMap);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Expression> it = params.iterator();

        if(method.getName().equals("<init>")) {
            sb.append("new ").append(method.ownerName());
        } else {
            if (method.isStatic()) {
                String owner = method.ownerName();
                if(!owner.isEmpty())
                    sb.append(method.ownerName()).append('.');
            } else
                sb.append(it.next()).append('.');
            sb.append(method.getName());
        }
        sb.append('(');

        int i = 0;
        while(it.hasNext()) {
            if(i > 0) sb.append(", ");

            TypeRef paramType = method.params.get(i++).type;
            Expression exp = it.next();
            if(TypeRef.printCL) {//add address of/deference as needed to match pointers
                TypeRef callType = exp.returnType();
                if(paramType.pointer != callType.pointer) {
                    if(exp.precedence() >= 3)
                        exp = new Parentheses(exp);
                    if(paramType.pointer > callType.pointer)
                        sb.append('&');
                    else if(paramType.pointer < callType.pointer)
                        sb.append('*');
                }
            }
            sb.append(exp);
        }

        sb.append(')');
        return sb.toString();
    }

    @Override
    public int precedence() {
        return 2;
    }

    @Override
    public MethodCall copy(Scope scope) {
        List<Expression> copies = new LinkedList<>();
        for(Expression exp : params)
            copies.add(exp.copy(scope));

        return new MethodCall(method, copies);
    }
}
