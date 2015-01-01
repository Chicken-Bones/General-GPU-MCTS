package gpuproj.translator;

import gpuproj.srctree.MethodSymbol;
import gpuproj.srctree.Symbol;
import gpuproj.srctree.TypeIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultMethodRemapper implements MethodRemapper
{
    public static final DefaultMethodRemapper instance = new DefaultMethodRemapper();

    public Map<String, String> map = new HashMap<>();

    private DefaultMethodRemapper() {
    }

    @Override
    public MethodSymbol map(MethodSymbol method) {
        String mapped = map.get(method.fullname);
        if(mapped != null) {
            List<Symbol> symbols = TypeIndex.instance().resolve(mapped, Symbol.METHOD_SYM);
            return MethodSymbol.match((List)symbols, method.params);
        }

        return method;
    }
}
