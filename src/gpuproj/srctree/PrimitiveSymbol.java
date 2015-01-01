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

    public static final PrimitiveSymbol VOID = new PrimitiveSymbol("void", 'V');
    public static final PrimitiveSymbol BOOLEAN = new PrimitiveSymbol("boolean", 'Z');
    public static final PrimitiveSymbol CHAR = new PrimitiveSymbol("char", 'C');
    public static final PrimitiveSymbol DOUBLE = new PrimitiveSymbol("double", 'D');
    public static final PrimitiveSymbol FLOAT = new PrimitiveSymbol("float", 'F', DOUBLE);
    public static final PrimitiveSymbol LONG = new PrimitiveSymbol("long", 'J', FLOAT);
    public static final PrimitiveSymbol INT = new PrimitiveSymbol("int", 'I', LONG);
    public static final PrimitiveSymbol SHORT = new PrimitiveSymbol("short", 'S', INT);
    public static final PrimitiveSymbol BYTE = new PrimitiveSymbol("byte", 'B', SHORT);

    public final char sig;
    public final PrimitiveSymbol wider;

    private PrimitiveSymbol(String name, char sig, PrimitiveSymbol wider) {
        super(name);
        this.sig = sig;
        this.wider = wider;
        nameMap.put(name, this);
        sigMap.put(sig, this);
        values.add(this);
    }

    private PrimitiveSymbol(String name, char sig) {
        this(name, sig, null);
    }

    @Override
    public ConcreteTypeSymbol concrete() {
        return this;
    }

    @Override
    public int symbolType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean isAssignableTo(ConcreteTypeSymbol type) {
        return type == this || wider != null && wider.isAssignableTo(type);
    }

    public static PrimitiveSymbol widen(PrimitiveSymbol p1, PrimitiveSymbol p2) {
        while(!p2.isAssignableTo(p1))
            p1 = p1.wider;

        return p1;
    }

    @Override
    public FieldSymbol getField(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MethodSymbol> getMethods(String name) {
        throw new UnsupportedOperationException();
    }
}
