package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SourceUtil
{
    public static String type2c(String type) {
        switch(type) {
            case "byte": return "char";
            case "boolean": return "uchar";
            case "char": return "ushort";
            default: return type;
        }
    }

    private static List<String> wider = Arrays.asList("char", "uchar", "short", "ushort", "int", "uint", "long", "ulong", "float", "double");
    public static TypeSymbol promoteNumeric(TypeSymbol type1, TypeSymbol type2) {
        return TypeIndex.instance.resolveType(wider.get(Math.max(wider.indexOf(type1.fullname), wider.indexOf(type2.fullname))));
    }

    public static int pointerLevel(String type) {
        int p = 0;
        while(type.charAt(type.length()-1-p) == '*') p++;
        return p;
    }

    /**
     * @return True if an operator of given precedence associates left to right (left operands are grouped first)
     */
    public static boolean assosciateRight(int precedence) {
        return precedence != 3 && precedence != 15;
    }

    public static String simpleName(String fullName) {
        int dot = fullName.lastIndexOf('.');
        return dot < 0 ? fullName : fullName.substring(dot+1);
    }

    public static String parentName(String fullName) {
        int dot = fullName.lastIndexOf('.');
        return dot < 0 ? "" : fullName.substring(0, dot);
    }

    public static String combineName(String parent, String name) {
        return parent.length() == 0 ? name : parent+'.'+name;
    }

    /**
     * set of all operator symbols, excluding brackets
     */
    public static Set<String> operator_symbols = new TreeSet<>(Arrays.asList(
            ".",
            "++", "--", "+", "-", "~", "!",
            "*", "/", "%",
            "<<", ">>", ">>>",
            "<", "<=", ">", ">=",
            "==", "!=",
            "&", "^", "|",
            "&&", "||",
            "?", ":",
            "*=", "/=", "+=", "-=", "%=", "<<=", ">>=", ">>>=", "&=", "^=", "|="
    ));

    public static int getModifier(String word) {
        switch(word) {
            case "public": return Modifier.PUBLIC;
            case "protected": return Modifier.PROTECTED;
            case "private": return Modifier.PRIVATE;
            case "abstract": return Modifier.ABSTRACT;
            case "static": return Modifier.STATIC;
            case "final": return Modifier.FINAL;
            case "transient": return Modifier.TRANSIENT;
            case "volatile": return Modifier.VOLATILE;
            case "synchronized": return Modifier.SYNCHRONIZED;
            case "native": return Modifier.NATIVE;
            default: return 0;
        }
    }
}
