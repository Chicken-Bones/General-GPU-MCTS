package gpuproj.srctree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DirectorySourceProvider implements SourceProvider
{
    public File base;

    public DirectorySourceProvider(File base) {
        this.base = base;
    }

    @Override
    public String provideSource(String path) {
        File file = new File(base, path);
        if(!file.exists()) return null;
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new FileReader(file));
            String s;
            while((s = r.readLine()) != null)
                sb.append(s).append('\n');

            r.close();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
