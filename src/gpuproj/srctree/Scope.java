package gpuproj.srctree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Resolves names to symbols.
 * For a given symbol type and name, parent scopes will only be delegated to if this scope's provider does not provide any symbols
 * Any resolved symbols for a given name and type are cached
 */
public final class Scope
{
    public interface ScopeProvider
    {
        /**
         * Add all symbols matching name and type to list. Will only be called once for a given name/type pair.
         * @param name The name to resolve symbols for. Will most likely be simple. Global names will generally be delegated to TypeIndex
         * @param type The type of symbol to resolve, only add symbols with a matching symbolType to list. Guaranteed to have only one set bit
         * @param list The list to add resolved symbols to
         */
        public void resolveOnce(String name, int type, List<Symbol> list);

        /**
         * Paired scope instance
         */
        public Scope scope();
    }

    private class SymbolCache
    {
        List[] lists = new LinkedList[Symbol.TYPE_COUNT];

        List<Symbol> resolve(String name, int types) {
            List<Symbol> single = null;
            List<Symbol> combined = null;
            for(int i = 0; i < Symbol.TYPE_COUNT; i++) {
                if((types & 1<<i) == 0) continue;
                List<Symbol> list = resolve_i(name, i);
                if(single == null)
                    single = list;
                else {
                    if(combined == null)
                        combined = new LinkedList<Symbol>(single);
                    combined.addAll(list);
                }
            }

            return combined != null ? combined : single;
        }

        private List<Symbol> resolve_i(String name, int i) {
            if(lists[i] == null) {
                List<Symbol> list = lists[i] = new LinkedList();
                LinkedList<Symbol> temp = new LinkedList<Symbol>();
                resolveOnce(name, 1 << i, temp);
                for(Symbol sym : temp)
                    if(!list.contains(sym))//remove duplicates
                        list.add(sym);
            }

            return lists[i];
        }

        private void resolveOnce(String name, int types, List<Symbol> list) {
            provider.resolveOnce(name, types, list);
            if(list.isEmpty() && parent != null)
                list.addAll(parent.resolve(name, types));
        }

        void add(Symbol sym) {
            for(int i = 0; i < Symbol.TYPE_COUNT; i++) {
                if(sym.symbolType() == 1<<i) {
                    if(lists[i] == null)
                        lists[i] = new LinkedList();
                    if(!lists[i].contains(sym))
                        lists[i].add(sym);
                }
            }
        }
    }

    public final Scope parent;
    public final ScopeProvider provider;
    private Map<String, SymbolCache> cache = new HashMap<String, SymbolCache>();

    public Scope(Scope parent, ScopeProvider provider) {
        this.parent = parent;
        this.provider = provider;
    }

    public Scope(ScopeProvider provider) {
        this(TypeIndex.scope, provider);
    }

    /**
     * Resolves all symbols matching name and types within this scope
     * @param name The name to resolve a symbol for
     * @param types A mask of symbol types
     */
    public List<Symbol> resolve(String name, int types) {
        SymbolCache symbols = cache.get(name);
        if(symbols == null)
            cache.put(name, symbols = new SymbolCache());

        return symbols.resolve(name, types);
    }

    /**
     * Wrapper for resolve, throws an exception if list contains more than 1 element.
     * @param name The name to resolve a symbol for
     * @param types A mask of symbol types
     * @return The single resolved element, or null if none were resolved
     */
    public Symbol resolve1(String name, int types) {
        List<Symbol> list = resolve(name, types);
        if(list.size() > 1) throw new IllegalStateException("Resolved more than one symbol for: "+name);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Adds a symbol to the cache under a given name
     */
    public void cache(Symbol sym, String name) {
        SymbolCache symbols = cache.get(name);
        if(symbols == null)
            cache.put(name, symbols = new SymbolCache());
        symbols.add(sym);
    }

    /**
     * Finds the ClassSymbol enclosing this scope.
     * Will throw a NullPointerException if neither this scope, nor any of it's parents have a ClassSymbol provider
     */
    public ClassSymbol thisClass() {
        return provider instanceof ClassSymbol ? (ClassSymbol) provider : parent.thisClass();
    }
}
