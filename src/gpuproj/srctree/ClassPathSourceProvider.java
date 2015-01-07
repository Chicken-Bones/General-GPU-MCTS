package gpuproj.srctree;

import java.io.*;

public class ClassPathSourceProvider implements SourceProvider
{
    public String prefix;

    public ClassPathSourceProvider(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String provideSource(String path) {
        if(!prefix.isEmpty())
            path = prefix+path;

        InputStream in = getClass().getResourceAsStream(path);
        if(in == null)
            return null;

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String s;
            while((s = r.readLine()) != null)
                sb.append(s).append('\n');

            in.close();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
