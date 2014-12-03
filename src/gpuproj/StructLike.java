package gpuproj;

public interface StructLike<T>
{
    /**
     * Set all fields of this to other
     * @return this
     */
    public T set(T other);

    /**
     * @return A copy of this, equal to new T().set(this)
     */
    public T copy();
}
