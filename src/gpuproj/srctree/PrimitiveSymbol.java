package gpuproj.srctree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * one of byte, short, char, int, long, float, double, boolean, void
 */
public class PrimitiveSymbol extends TypeSymbol
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
    public TypeSymbol concrete() {
        return this;
    }

    @Override
    public int getType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String signature() {
        return String.valueOf(sig);
    }
}
