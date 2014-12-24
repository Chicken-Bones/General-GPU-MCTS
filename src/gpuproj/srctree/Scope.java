package gpuproj.srctree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Scope
{
    public interface ScopeProvider
    {
        /**
         * Add all symbols matching name and type to list
         */
        public void resolveOnce(String name, int type, List<Symbol> list);
    }

    public final Scope parent;
    public final ScopeProvider provider;
    public Map<String, List<Symbol>> cache = new HashMap<>();

    public Scope(Scope parent, ScopeProvider provider) {
        this.parent = parent;
        this.provider = provider;
    }

    public Scope(ScopeProvider provider) {
        this(TypeIndex.instance.scope, provider);
    }

    private void resolveOnce(String name, int types, List<Symbol> list) {
        provider.resolveOnce(name, types, list);
        if(list.isEmpty() && parent != null)
            list.addAll(parent.resolve(name, types));
    }

    /**
     * Resolves name to a symbol within this scope matching type. Returns null if not found.
     * Uses a map to cache all symbols resolved in this scope
     */
    public List<Symbol> resolve(String name, int types) {
        List<Symbol> symbols = cache.get(name);
        if(symbols == null) {
            cache.put(name, symbols = new LinkedList<>());
            resolveOnce(name, types, symbols);
        }
        return symbols;
    }

    public void cache(Symbol sym) {
        List<Symbol> symbols = cache.get(sym.fullname);
        if(symbols == null)
            cache.put(sym.fullname, symbols = new LinkedList<>());
        symbols.add(sym);
    }

    public Symbol resolve1(String name, int types) {
        List<Symbol> list = resolve(name, types);
        return list.isEmpty() ? null : list.get(0);
    }
}
