package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class MethodSymbol extends GlobalSymbol implements ScopeProvider
{
    public final Object source;
    public final ReferenceSymbol owner;
    public final Scope scope;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<>();
    public TypeRef returnType;
    public List<LocalSymbol> params = new LinkedList<>();
    public Block body;

    public MethodSymbol(String fullname, ReferenceSymbol owner, Object source) {
        super(fullname);
        this.owner = owner;
        this.source = source;
        scope = new Scope(owner.scope, this);
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if((type & TYPE_PARAM) != 0) {
            for(TypeParam p : typeParams)
                if(p.alias.equals(name))
                    list.add(p);
        }
        if((type & FIELD_SYM) != 0) {
            for(LocalSymbol p : params)
                if(p.name.equals(name))
                    list.add(p);
        }
    }

    @Override
    public int getType() {
        return Symbol.METHOD_SYM;
    }

    @Override
    public String toString() {
        if(returnType != null) {
            StringBuilder sb = new StringBuilder();
            if(modifiers != 0)
                sb.append(Modifier.toString(modifiers)).append(' ');
            if(!typeParams.isEmpty())
                sb.append("<").append(SourceUtil.listString(typeParams)).append("> ");
            sb.append(returnType).append(' ').append(name);
            sb.append('(').append(SourceUtil.listString(params)).append(')');
            return sb.toString();
        }

        return fullname;
    }

    /*public String signature() {
        if(returnType == null)//not fully initialised
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (LocalSymbol param : params)
            sb.append(param.type.signature());
        sb.append(')');
        sb.append(returnType.signature());
        return sb.toString();
    }*/
}
