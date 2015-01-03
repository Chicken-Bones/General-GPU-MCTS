package gpuproj.srctree;

import java.lang.annotation.Annotation;

/**
 * A symbol that can have annotations.
 * In this api, annotations are not read from source, but are obtained via the corresponding reflection element.
 * Annotations cannot be obtained for local variables or parameters
 */
public interface AnnotatedSymbol extends Symbol
{
    /**
     * @param type The annotation type class constant. eg. Override.class
     * @return An instance of type if the annotation exists, otherwise null
     */
    public <A extends Annotation> A getAnnotation(Class<A> type);
}
