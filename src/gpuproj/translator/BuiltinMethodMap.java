package gpuproj.translator;

import gpuproj.srctree.MethodSymbol;
import gpuproj.srctree.TypeIndex;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class BuiltinMethodMap
{
    public static Map<String, String> map = new HashMap<>();
    private static Map<String, MethodSymbol> built = new HashMap<>();

    static {
        map.put("java.lang.Math.min(II)I", "min");
        map.put("java.lang.Math.min(JJ)J", "min");
        map.put("java.lang.Math.max(II)I", "max");
        map.put("java.lang.Math.max(JJ)J", "max");
        map.put("java.lang.Math.abs(I)I", "abs");
        map.put("java.lang.Math.abs(J)J", "abs");
        map.put("java.lang.Long.bitCount", "popcount");
        map.put("java.lang.Integer.bitCount", "popcount");
        map.put("java.lang.Long.numberOfLeadingZeros", "clz");
        map.put("java.lang.Integer.numberOfLeadingZeros", "clz");
        map.put("java.lang.Math.acos", "acos");
        map.put("java.lang.Math.asin", "asin");
        map.put("java.lang.Math.atan", "atan");
        map.put("java.lang.Math.atan2", "atan2");
        map.put("java.lang.Math.ceil", "ceil");
        map.put("java.lang.Math.cbrt", "cbrt");
        map.put("java.lang.Math.cos", "cos");
        map.put("java.lang.Math.cosh", "cosh");
        map.put("java.lang.Math.exp", "exp");
        map.put("java.lang.Math.expm1", "expm1");
        map.put("java.lang.Math.abs(F)F", "fabs");
        map.put("java.lang.Math.abs(D)D", "fabs");
        map.put("java.lang.Math.max(FF)F", "fmax");
        map.put("java.lang.Math.max(DD)D", "fmax");
        map.put("java.lang.Math.min(FF)F", "fmin");
        map.put("java.lang.Math.min(DD)D", "fmin");
        map.put("java.lang.Math.hypot", "hypot");
        map.put("java.lang.Math.log", "log");
        map.put("java.lang.Math.log10", "log10");
        map.put("java.lang.Math.log1p", "log1p");
        map.put("java.lang.Math.pow", "pow");
        map.put("java.lang.Math.rint", "rint");
        map.put("java.lang.Math.round", "round");
        map.put("java.lang.Math.sin", "sin");
        map.put("java.lang.Math.sinh", "sinh");
        map.put("java.lang.Math.sqrt", "sqrt");
        map.put("java.lang.Math.tan", "tan");
        map.put("java.lang.Math.tanh", "tanh");
    }

    /**
     * Gets or creates a builtin symbol for name
     */
    public static MethodSymbol get(String name) {
        MethodSymbol sym = built.get(name);
        if(sym == null) {
            sym = new MethodSymbol(name, TypeIndex.scope, null);
            sym.modifiers |= Modifier.PUBLIC | Modifier.STATIC;
            built.put(name, sym);
        }

        return sym;
    }

    public static String map(MethodSymbol method) {
        String mapped = map.get(method.fullname);
        if(mapped == null)
            mapped = map.get(method.fullname+method.signature());
        return mapped;
    }
}
