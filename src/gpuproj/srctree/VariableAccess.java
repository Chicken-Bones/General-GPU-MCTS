package gpuproj.srctree;

/**
 * Field or Local access
 */
public class VariableAccess extends Expression
{
    public Variable var;
    /**
     * Exp will be null for static fields and locals
     */
    public Expression exp;

    public VariableAccess(Variable var, Expression exp) {
        this.var = var;
        this.exp = exp;
    }

    public VariableAccess(Variable var) {
        this(var, null);
    }

    @Override
    public TypeRef returnType() {
        return var.getType();
    }

    @Override
    public String toString() {
        if(var instanceof LocalSymbol)
            return var.getName();

        FieldSymbol field = (FieldSymbol) var;
        if(field.isStatic())
            return field.fullname;

        return exp + "." + field.getName();
    }

    @Override
    public int precedence() {
        return 2;
    }

    @Override
    public boolean lvalue() {
        return true;
    }
}
