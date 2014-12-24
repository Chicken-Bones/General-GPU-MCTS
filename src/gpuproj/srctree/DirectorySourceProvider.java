package gpuproj.srctree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DirectorySourceProvider implements SourceProvider
{
    public File base;

    public DirectorySourceProvider(File base) {
        this.base = base;
    }

    @Override
    public boolean packageExists(String path) {
        File dir = new File(base, path);
        return dir.exists() && dir.isDirectory();
    }

    @Override
    public String findClass(String path) {
        File file = new File(base, path);
        if(!file.exists()) return null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
