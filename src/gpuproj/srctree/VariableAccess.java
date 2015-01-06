package gpuproj.srctree;

/**
 * Field or Local access
 */
public class VariableAccess extends Expression
{
    public Variable var;
    /**
     * Exp will be null for static accessors and locals
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

        String op = TypeRef.printCL && exp.returnType().pointer == 1 ? "->" : ".";
        return exp + op + field.getName();
    }

    @Override
    public int precedence() {
        return 2;
    }

    @Override
    public boolean lvalue() {
        return true;
    }

    @Override
    public VariableAccess copy(Scope scope) {
        Variable copy = var;
        if(copy instanceof LocalSymbol)
            copy = (Variable) scope.resolve1(copy.getName(), Symbol.LOCAL_SYM);

        return new VariableAccess(copy, exp == null ? null : exp.copy(scope));
    }
}
