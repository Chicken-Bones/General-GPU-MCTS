package gpuproj.srctree;

public abstract class Expression
{
    public abstract String returnType();

    /**
     * @return Recursively evaluated valid source code for this expression
     */
    public abstract String print();

    /**
     * @return An integer representing the precedence of this expression, based on http://en.cppreference.com/w/cpp/language/operator_precedence
     * If a higher precedence operation wants to use this as an argument, it must be bracketed
     */
    public abstract int precedence();
}
