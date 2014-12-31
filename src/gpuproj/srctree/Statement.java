package gpuproj.srctree;

import javax.swing.plaf.nimbus.State;

public class Statement
{

    //blocks just indent everything they get
    //control flow indents substatements, but blocks use the formatted brace structure
    //case block is special

    public static boolean endsWith(StringBuilder sb, String s) {
        return s.length() <= sb.length() && sb.substring(sb.length()-s.length()).equals(s);
    }

    /**
     * Formats substatements
     */
    public static void printSub(StringBuilder sb, Statement stmt) {
        if(stmt instanceof Block)
            sb.append(' ').append(stmt);
        else if(stmt instanceof IfStatement && endsWith(sb, "else"))
            sb.append(' ').append(stmt);
        else {
            sb.append('\n').append(indent(stmt.toString()));
            finishStatement(sb);
        }
    }

    public static String indent(String s) {
        StringBuilder sb = new StringBuilder();
        String[] lines = s.split("\n");
        for(int i = 0; i < lines.length; i++) {
            if(i != 0) sb.append('\n');
            sb.append("    ").append(lines[i]);
        }
        return sb.toString();
    }

    public static void finishStatement(StringBuilder sb) {
        if(!endsWith(sb, "}") && !endsWith(sb, ";"))
            sb.append(';');
    }

    /**
     * Formats sb for a continued substatement (do-while, if-else)
     */
    public static void continueSub(StringBuilder sb) {
        if(endsWith(sb, "}"))
            sb.append(' ');
        else
            sb.append('\n');
    }
}
