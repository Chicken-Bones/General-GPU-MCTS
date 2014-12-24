package gpuproj.srctree;

/**
 * Unresolved annotation
 */
public class AnnotationSymbol
{
    public final ClassSymbol type;
    public final String params;

    public AnnotationSymbol(ClassSymbol type, String params) {
        this.type = type;
        this.params = params;
    }
}
