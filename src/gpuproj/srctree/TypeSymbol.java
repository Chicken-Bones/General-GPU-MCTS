package gpuproj.srctree;

/**
 * Class, primitive, or generic parameter
 */
public abstract class TypeSymbol implements Symbol
{
    public String fullname;
    private ArraySymbol array;

    public TypeSymbol(String fullname) {
        this.fullname = fullname;
    }

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
     * @return The a concrete type (reference, primitive, or array) that any valid instance of this symbol could be cast to
     */
    public TypeSymbol concrete() {
        return this;
    }

    public boolean isAssignableTo(TypeSymbol type) {
        return type == this;
    }

    public ArraySymbol array() {
        if(array == null)
            array = new ArraySymbol(this);

        return array;
    }

    public Expression defaultValue() {
        return new Literal("null");
    }

    public abstract String signature();
}
