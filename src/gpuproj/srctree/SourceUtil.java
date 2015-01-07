package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SourceUtil
{
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

    public static String firstName(String fullName) {
        int dot = fullName.indexOf('.');
        return dot < 0 ? fullName : fullName.substring(0, dot);
    }

    /**
     * set of all operator symbols, excluding brackets
     */
    public static Set<String> operator_symbols = new TreeSet<String>(Arrays.asList(
            ".",
            "++", "--", "+", "-", "~", "!",
            "*", "/", "%",
            "<<", ">>", ">>>",
            "<", "<=", ">", ">=",
            "==", "!=",
            "&", "^", "|",
            "&&", "||",
            "?", ":",
            "=", "*=", "/=", "+=", "-=", "%=", "<<=", ">>=", ">>>=", "&=", "^=", "|="
    ));

    public static int getModifier(String word) {
        if (word.equals("public")) return Modifier.PUBLIC;
        if (word.equals("protected")) return Modifier.PROTECTED;
        if (word.equals("private")) return Modifier.PRIVATE;
        if (word.equals("abstract")) return Modifier.ABSTRACT;
        if (word.equals("static")) return Modifier.STATIC;
        if (word.equals("final")) return Modifier.FINAL;
        if (word.equals("transient")) return Modifier.TRANSIENT;
        if (word.equals("volatile")) return Modifier.VOLATILE;
        if (word.equals("synchronized")) return Modifier.SYNCHRONIZED;
        if (word.equals("native")) return Modifier.NATIVE;
        return 0;
    }

    public static String listString(List<?> list) {
        return listString(list, ", ");
    }

    public static String listString(List<?> list, String sep) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < list.size(); i++) {
            if(i > 0) sb.append(sep);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /**
     * Determines whether statement is a method or a field by which of '=', '{' or '(' comes first
     * @return Symbol.METHOD_SYM or Symbol.FIELD_SYM
     */
    public static int methodOrField(String stmt) {
        int equals = stmt.indexOf('=');
        int brace = stmt.indexOf('{');
        int bracket = stmt.indexOf('(');

        if(equals < 0) equals = Integer.MAX_VALUE;
        if(brace < 0) brace = Integer.MAX_VALUE;
        if(bracket < 0) bracket = Integer.MAX_VALUE;

        if(bracket < equals || brace < equals) return Symbol.METHOD_SYM;
        return Symbol.FIELD_SYM;
    }
}
