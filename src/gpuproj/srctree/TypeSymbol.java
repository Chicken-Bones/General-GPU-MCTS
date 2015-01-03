package gpuproj.srctree;

/**
 * Class, array, primitive, or generic parameter
 */
public abstract class TypeSymbol implements Symbol
{
    public String fullname;
    /**
     * Symbol instance for an array of this type.
     */
    private ArraySymbol array;

    public TypeSymbol(String fullname) {
        this.fullname = fullname;
    }

    /**
     * @return The simple name of this symbol
     */
    public String getName() {
        return SourceUtil.simpleName(fullname);
    }

    @Override
    public String globalName() {
        return isConcrete() ? fullname : null;
    }

    /**
     * @return true if this class has a corresponding .class file or is an array of a concrete type
     */
    public abstract boolean isConcrete();

    /**
     * @return The a concrete type (reference, primitive, or array) that any instance of this symbol could be cast to
     */
    public TypeSymbol concrete() {
        return this;
    }

    /**
     * @return True if this symbol can be cast to type
     */
    public boolean isAssignableTo(TypeSymbol type) {
        return type == this;
    }

    /**
     * @return A symbol representing an array of this type.
     */
    public ArraySymbol array() {
        if(array == null)
            array = new ArraySymbol(this);

        return array;
    }

    /**
     * @return The default expression for fields with this type and no initialiser
     */
    public Expression defaultValue() {
        return new Literal("null");
    }

    /**
     * @return The compiled type signature of this (must equal concrete().signature())
     */
    public abstract String signature();

    /**
     * @return The runtime name of this symbol, uses $ instead of . for inner class separators (must equal concrete().runtimeName()).
     */
    public abstract String runtimeName();

    /**
     * @return The runtime class instance of this symbol (must equal concrete().runtimeClass())
     */
    public abstract Class<?> runtimeClass();
}
