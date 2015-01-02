package gpuproj.srctree;

import java.lang.annotation.Annotation;

public interface AnnotatedSymbol extends Symbol
{
    public Annotation getAnnotation(Class<? extends Annotation> type);
}
