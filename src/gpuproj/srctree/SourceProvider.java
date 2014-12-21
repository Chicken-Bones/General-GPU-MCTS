package gpuproj.srctree;

public interface SourceProvider
{
    public boolean packageExists(String path);

    /**
     * @param path A path to the java class file, eg 'java/lang/Long.java'
     * @return The source file as a string or null if not found
     */
    public String findClass(String path);
}
