package gpuproj;

public interface StructLike<T>
{
    /**
     * Set all accessors of this to other
     * @return this
     */
    public T set(T other);

    /**
     * @return A copy of this, equal to new T().set(this)
     */
    public T copy();
}
