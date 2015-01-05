package gpuproj.srctree;

public class LocalSymbol extends Statement implements Variable
{
    public TypeRef type;
    public String name;
    public Expression init;

    public LocalSymbol(TypeRef type, String name, Expression init) {
        this.type = type;
        this.name = name;
        this.init = init;
    }

    public LocalSymbol(TypeRef type, String name) {
        this(type, name, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(' ').append(name);
        if(init != null)
            sb.append(" = ").append(init);

        return sb.toString();
    }

    @Override
    public int symbolType() {
        return Symbol.LOCAL_SYM;
    }

    @Override
    public TypeRef getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Expression initialiser() {
        return init;
    }

    @Override
    public String globalName() {
        return null;
    }

    @Override
    public LocalSymbol copy(Scope scope) {
        return new LocalSymbol(type, name, init == null ? null : init.copy(scope));
    }
}
