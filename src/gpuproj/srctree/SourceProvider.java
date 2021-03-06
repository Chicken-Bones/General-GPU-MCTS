package gpuproj.srctree;

public interface SourceProvider
{
    /**
     * @param path A path to the java class file, eg 'java/lang/Long.java'
     * @return The source file as a string or null if not found
     */
    public String provideSource(String path);
}
