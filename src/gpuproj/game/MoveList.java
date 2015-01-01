package gpuproj.game;

import gpuproj.util.Portable;
import gpuproj.StructLike;
import gpuproj.translator.OCLGlobal;
import gpuproj.translator.OCLGlobalConverter;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Special GPU portable list class for storing moves.
 * Backed by a constant fixed length array which is filled using the default constructor.
 * Only supports add and set operations which mutate the instance in the array
 */
@OCLGlobal(OCLGlobalConverter.class)
public class MoveList<T extends StructLike<T>>
{
    private final Object[] arr;
    private short size;

    public MoveList(int capacity, Class<T> type) {
        arr = new Object[capacity];
        try {
            Constructor<T> c = type.getConstructor();
            for(int i = 0; i < capacity; i++)
                arr[i] = c.newInstance();
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Unable to instantiate move list ("+capacity+", "+type+")", e);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Adds a new element to this list, by calling set on the next available array element.
     * If this list is full, overwrites a random element.
     * @return true
     */
    public boolean add(T t) {
        if(size == arr.length)
            ((T) arr[Portable.randInt(arr.length)]).set(t);
        else
            ((T) arr[size++]).set(t);
        return true;
    }

    public void clear() {
        size = 0;
    }

    public T get(int index) {
        if(index >= size) throw new IndexOutOfBoundsException("cap: "+arr.length+". index: "+index);
        return (T) arr[index];
    }

    public T set(int index, T element) {
        if(index >= size) throw new IndexOutOfBoundsException("cap: "+arr.length+". index: "+index);
        T t = (T)arr[index];
        t.set(element);
        return t;
    }

    public List<T> toList() {
        ArrayList<T> list = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
            list.add(((T)arr[i]).copy());
        return list;
    }
}
