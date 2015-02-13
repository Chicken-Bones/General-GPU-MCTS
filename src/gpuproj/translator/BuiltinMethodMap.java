package gpuproj.translator;

import gpuproj.srctree.*;
import gpuproj.translator.JavaTranslator.MethodDecl;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class BuiltinMethodMap
{
    public static Map<String, String> map = new HashMap<String, String>();
    private static Map<String, String> nvidiaASM = new HashMap<String, String>();
    private static Map<PrimitiveSymbol, String> regASMMap = new HashMap<PrimitiveSymbol, String>();

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

        nvidiaASM.put("popcount(J)I", "popc.b64 %0, %1");
        regASMMap.put(PrimitiveSymbol.SHORT, "h");
        regASMMap.put(PrimitiveSymbol.INT, "r");
        regASMMap.put(PrimitiveSymbol.LONG, "l");
        regASMMap.put(PrimitiveSymbol.FLOAT, "f");
        regASMMap.put(PrimitiveSymbol.DOUBLE, "d");
    }

    public static MethodSymbol get(String name, MethodSymbol sym, JavaTranslator t) {
        sym = sym.copySig(name, t.scope(), JavaTranslator.builtinSource);
        sym.modifiers = Modifier.STATIC;

        String asm = nvidiaASM.get(name+sym.signature());
        if(asm != null && t.program.env.vendor.equals("NVIDIA Corporation")) {
            TypeRef.printCL = true;
            StringBuilder body = new StringBuilder().append("{\n");
            body.append("    ").append(sym.returnType).append(" ret;\n");

            body.append("    asm(\"").append(asm).append(";\"");

            if(sym.returnType.type != PrimitiveSymbol.VOID)
                body.append(" : \"=").append(regASMMap.get(sym.returnType.type)).append("\"(ret)");

            for(LocalSymbol param : sym.params)
                body.append(" : \"").append(regASMMap.get(param.type.type)).append("\"(").append(param.name).append(")");

            body.append(");\n");
            body.append("    return ret;\n}");
            TypeRef.printCL = false;

            sym.body = new CStatement(body.toString());
            MethodDecl w = new MethodDecl(sym);
            t.program.declare(w);
            t.program.implement(w);
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
