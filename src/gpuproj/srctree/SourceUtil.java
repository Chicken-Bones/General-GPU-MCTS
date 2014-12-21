package gpuproj.srctree;

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
    public static String promoteNumeric(String type1, String type2) {
        return wider.get(Math.max(wider.indexOf(type1), wider.indexOf(type2)));
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
}
