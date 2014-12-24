package gpuproj.srctree;

import java.util.HashMap;
import java.util.Map;

public final class Scope
{
    public interface ScopeProvider
    {
        /**
         * Attempt to resolve a symbol using just this scope (not parents)
         * @return A symbol if found, or null
         */
        public Symbol resolveSingle(String name, int type);
    }

    public final Scope parent;
    public final ScopeProvider provider;
    public Map<String, Symbol> cache = new HashMap<>();

    public Scope(Scope parent, ScopeProvider provider) {
        this.parent = parent;
        this.provider = provider;
    }

    public Scope(ScopeProvider provider) {
        this(TypeIndex.instance.scope, provider);
    }

    private Symbol resolveFirst(String name, int type) {
        Symbol sym = provider.resolveSingle(name, type);
        return sym != null ? sym : parent != null ? parent.resolve(name, type) : null;
    }

    /**
     * Resolves name to a symbol within this scope matching type. Returns null if not found.
     * Uses a map to cache all symbols resolved in this scope
     */
    public Symbol resolve(String name, int type) {
        Symbol res = cache.get(name);
        if(res == null)
            cache.put(name, res = resolveFirst(name, type));
        return res;
    }

    public void cache(Symbol sym) {
        cache.put(sym.fullname, sym);
    }
}
