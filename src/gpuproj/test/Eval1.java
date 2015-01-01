package gpuproj.test;

import gpuproj.test.Eval2.*;

public abstract class Eval1
{
    public static class Inner1
    {
    }

    public abstract void method(Inner2 param);

    private void test(int a, double b){}
    private void test(double a, int b){}

    private void test() {
        test(0, 0D);
    }
}
