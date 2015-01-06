package gpuproj.test;

public abstract class Eval1
{
    public static class Top<A>
    {
    }

    public static class Next<B> extends Top<B>
    {

    }

    public static class Bottom extends Next<Object>
    {

    }

    public abstract <T> void method(Top<T> param);
    //supply an instance of Bottom
    //match Bottom
}
