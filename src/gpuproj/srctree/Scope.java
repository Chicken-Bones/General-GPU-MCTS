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

    public class SymbolCache
    {
        public List[] lists = new LinkedList[Symbol.TYPE_COUNT];

        public List<Symbol> resolve(String name, int types) {
            List<Symbol> single = null;
            List<Symbol> combined = null;
            for(int i = 0; i < Symbol.TYPE_COUNT; i++) {
                if((types & 1<<i) == 0) continue;
                List<Symbol> list = resolve_i(name, i);
                if(single == null)
                    single = list;
                else {
                    if(combined == null)
                        combined = new LinkedList<>(single);
                    combined.addAll(list);
                }
            }

            return combined != null ? combined : single;
        }

        private List<Symbol> resolve_i(String name, int i) {
            if(lists[i] == null) {
                List<Symbol> list = lists[i] = new LinkedList();
                LinkedList<Symbol> temp = new LinkedList<>();
                resolveOnce(name, 1 << i, temp);
                for(Symbol sym : temp)
                    if(!list.contains(sym))//remove duplicates
                        list.add(sym);
            }

            return lists[i];
        }

        public void add(Symbol sym) {
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
    public Map<String, SymbolCache> cache = new HashMap<>();

    public Scope(Scope parent, ScopeProvider provider) {
        this.parent = parent;
        this.provider = provider;
    }

    public Scope(ScopeProvider provider) {
        this(TypeIndex.instance().scope, provider);
    }

    private void resolveOnce(String name, int types, List<Symbol> list) {
        provider.resolveOnce(name, types, list);
        if(list.isEmpty() && parent != null)
            list.addAll(parent.resolve(name, types));
    }

    /**
     * Resolves name to a symbol within this scope matching type.
     * Uses a map to cache all symbols resolved in this scope
     */
    public List<Symbol> resolve(String name, int types) {
        SymbolCache symbols = cache.get(name);
        if(symbols == null)
            cache.put(name, symbols = new SymbolCache());

        return symbols.resolve(name, types);
    }

    public void cache(Symbol sym, String name) {
        SymbolCache symbols = cache.get(name);
        if(symbols == null)
            cache.put(name, symbols = new SymbolCache());
        symbols.add(sym);
    }

    public Symbol resolve1(String name, int types) {
        List<Symbol> list = resolve(name, types);
        if(list.size() > 1) throw new IllegalStateException("Resolved more than one symbol for: "+name);
        return list.isEmpty() ? null : list.get(0);
    }
}
