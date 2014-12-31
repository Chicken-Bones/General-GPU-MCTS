package gpuproj.srctree;

import java.util.*;

/**
 * one of byte, short, char, int, long, float, double, boolean, void
 */
public class PrimitiveSymbol extends ConcreteTypeSymbol
{
    public static Map<String, PrimitiveSymbol> nameMap = new HashMap<>();
    public static Map<Character, PrimitiveSymbol> sigMap = new HashMap<>();
    public static List<PrimitiveSymbol> values = new ArrayList<>();

    public static final PrimitiveSymbol BYTE = new PrimitiveSymbol("byte", 'B');
    public static final PrimitiveSymbol SHORT = new PrimitiveSymbol("short", 'S');
    public static final PrimitiveSymbol CHAR = new PrimitiveSymbol("char", 'C');
    public static final PrimitiveSymbol INT = new PrimitiveSymbol("int", 'I');
    public static final PrimitiveSymbol LONG = new PrimitiveSymbol("long", 'J');
    public static final PrimitiveSymbol FLOAT = new PrimitiveSymbol("float", 'F');
    public static final PrimitiveSymbol DOUBLE = new PrimitiveSymbol("double", 'D');
    public static final PrimitiveSymbol BOOLEAN = new PrimitiveSymbol("boolean", 'Z');
    public static final PrimitiveSymbol VOID = new PrimitiveSymbol("void", 'V');

    public final char sig;

    private PrimitiveSymbol(String name, char sig) {
        super(name);
        this.sig = sig;
        nameMap.put(name, this);
        sigMap.put(sig, this);
        values.add(this);
    }

    @Override
    public ConcreteTypeSymbol concrete() {
        return this;
    }

    @Override
    public int getType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public List<FieldSymbol> getFields() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return name;
    }
}
