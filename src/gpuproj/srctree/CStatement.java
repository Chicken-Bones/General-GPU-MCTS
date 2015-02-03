package gpuproj.srctree;

public class CStatement extends Statement
{
    public String source;

    public CStatement(String source) {
        this.source = source;
    }

    @Override
    public Statement copy(Scope scope) {
        return new CStatement(source);
    }

    @Override
    public String toString() {
        return source;
    }
}
