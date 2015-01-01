package gpuproj.translator;

public @interface OCLGlobal
{
    Class<? extends OCLGlobalConverter> value();
}
