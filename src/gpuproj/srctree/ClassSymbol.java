package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ClassSymbol extends Symbol implements ScopeProvider
{
    public boolean inner;
    public int modifiers;
    public final String declaration;
    public List<ClassSymbol> innerClasses = new LinkedList<>();
    public List<FieldSymbol> fields = new LinkedList<>();
    public List<MethodSymbol> methods = new LinkedList<>();

    public ClassSymbol(String name, String declaration) {
        super(name);
        this.declaration = declaration;
        readSymbols();
    }

    private void readSymbols() {
        SourceReader r = new SourceReader(declaration);

    }

    @Override
    public Symbol resolveSingle(String name, int type) {
        return null;
    }

    public FieldSymbol resolveField(String name) {

    }
}
