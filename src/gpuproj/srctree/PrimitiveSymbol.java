package gpuproj.srctree;

import java.util.*;

/**
 * one of byte, short, char, int, long, float, double, boolean, void
 */
public class PrimitiveSymbol extends TypeSymbol
{
    public static Map<String, PrimitiveSymbol> nameMap = new HashMap<String, PrimitiveSymbol>();
    public static Map<Character, PrimitiveSymbol> sigMap = new HashMap<Character, PrimitiveSymbol>();
    public static List<PrimitiveSymbol> values = new ArrayList<PrimitiveSymbol>();

    public static final PrimitiveSymbol VOID = new PrimitiveSymbol(Void.TYPE, 'V');
    public static final PrimitiveSymbol BOOLEAN = new PrimitiveSymbol(Boolean.TYPE, 'Z');
    public static final PrimitiveSymbol CHAR = new PrimitiveSymbol(Character.TYPE, 'C');
    public static final PrimitiveSymbol DOUBLE = new PrimitiveSymbol(Double.TYPE, 'D');
    public static final PrimitiveSymbol FLOAT = new PrimitiveSymbol(Float.TYPE, 'F', DOUBLE);
    public static final PrimitiveSymbol LONG = new PrimitiveSymbol(Long.TYPE, 'J', FLOAT);
    public static final PrimitiveSymbol INT = new PrimitiveSymbol(Integer.TYPE, 'I', LONG);
    public static final PrimitiveSymbol SHORT = new PrimitiveSymbol(Short.TYPE, 'S', INT);
    public static final PrimitiveSymbol BYTE = new PrimitiveSymbol(Byte.TYPE, 'B', SHORT);

    public final Class<?> runtimeClass;
    public final char sig;
    public final PrimitiveSymbol wider;

    private PrimitiveSymbol(Class<?> runtimeClass, char sig, PrimitiveSymbol wider) {
        super(runtimeClass.getName());
        this.runtimeClass = runtimeClass;
        this.sig = sig;
        this.wider = wider;
        nameMap.put(getName(), this);
        sigMap.put(sig, this);
        values.add(this);
    }

    private PrimitiveSymbol(Class<?> runtimeClass, char sig) {
        this(runtimeClass, sig, null);
    }

    @Override
    public int symbolType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String toString() {
        return fullname;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public boolean isAssignableTo(TypeSymbol type) {
        return type == this || wider != null && wider.isAssignableTo(type);
    }

    @Override
    public Expression defaultValue() {
        switch(sig) {
            case 'Z': return new Literal("false");
            case 'C': return new Literal("0");
            case 'D': return new Literal("0D");
            case 'F': return new Literal("0F");
            case 'J': return new Literal("0L");
            case 'I': return new Literal("0");
            case 'S': return new Literal("0");
            case 'B': return new Literal("0");
            default: return super.defaultValue();
        }
    }

    @Override
    public String signature() {
        return String.valueOf(sig);
    }

    @Override
    public String runtimeName() {
        return fullname;
    }

    @Override
    public Class<?> runtimeClass() {
        return runtimeClass;
    }

    public static PrimitiveSymbol widen(PrimitiveSymbol p1, PrimitiveSymbol p2) {
        while(!p2.isAssignableTo(p1))
            p1 = p1.wider;

        return p1;
    }
}
