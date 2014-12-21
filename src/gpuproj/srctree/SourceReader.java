package gpuproj.srctree;

import java.util.Stack;

public class SourceReader
{
    private String source;
    private int pos;
    private Stack<Integer> marks = new Stack<>();

    public SourceReader(String source) {
        this.source = source;
    }

    public void mark() {
        marks.push(pos);
    }

    public void update() {
        if(!marks.isEmpty()) marks.pop();
        marks.push(pos);
    }

    public void reset() {
        pos = marks.pop();
    }

    public void drop() {
        marks.pop();
    }

    /**
     * Skips comments and whitespace to find code. Increments pos to point to the next code relevant character (may leave it where it is)
     */
    public void seekCode() {
        while(pos < source.length()) {
            char c = source.charAt(pos);
            if (Character.isWhitespace(c))  {
                pos++;
                continue;
            }

            if(c == '/') {
                char c2 = source.charAt(pos+1);
                if(c2 == '/') {//line comment
                    int idx = source.indexOf('\n', pos);
                    if(idx < 0) pos = source.length();
                    else pos = idx+1;
                } else if(c2 == '*') {//block comment
                    int idx = source.indexOf("*/", pos);
                    if (idx < 0) pos = source.length();
                    else pos = idx + 2;
                }
                continue;
            }

            break;
        }
    }

    private String readLiteral() {
        int start = pos;
        char c = source.charAt(pos);
        boolean hex = c == '0' && source.charAt(pos+1) == 'x';
        while(Character.isLetterOrDigit(c = source.charAt(++pos))) {
            if(!hex && (c == 'e' || c == 'E')) pos++;//accept the character after an exponent as valid
        }
        return source.substring(start, pos);
    }

    private String readIdentifier() {
        int start = pos;
        while(Character.isJavaIdentifierPart(source.charAt(++pos)));
        return source.substring(start, pos);
    }

    /**
     * Does not seek
     */
    public String readTo(char delim, boolean include) {
        int start = pos;
        pos = source.indexOf(';', pos)+1;
        return source.substring(start, include ? pos : pos-1);
    }

    private String readSymbol() {
        int start = pos++;
        while(SourceUtil.operator_symbols.contains(source.substring(start, pos+1))) pos++;//utilise the fact that all multi-char operators extend a valid operator
        return source.substring(start, pos);
    }

    /**
     * Seeks to the character after the closing quote of a string.
     * Assumes pos is positioned directly after the opening quote
     */
    private void skipString() {
        while(true) {
            char c = source.charAt(pos++);
            if(c == '\'') pos++;//skip the char after an escape
            else if(c == '"') break;
        }
    }

    /**
     * Seeks to the character after the closing quote of a char literal.
     * Assumes pos is positioned directly after the opening quote
     */
    private void skipChar() {
        if(source.charAt(pos) == '\'') pos++;
        pos+=2;
    }

    private String readPaired(char open, char close) {
        int start = pos;
        int level = 0;
        do {
            char c = source.charAt(pos++);
            if(c == '\'') skipChar();
            else if(c == '"') skipString();
            else if(c == open) level++;
            else if(c == close) level--;
        } while (level > 0);
        return source.substring(start, pos);
    }

    /**
     * Seeks to the start of code and returns the next element (word, number, bracket/brace pair or valid operator)
     */
    public String readElement() {
        seekCode();
        char c = source.charAt(pos);
        if(Character.isDigit(c))
            return readLiteral();
        if(Character.isJavaIdentifierStart(c))
            return readIdentifier();
        if(c == '(')
            return readPaired('(', ')');
        if(c == '[')
            return readPaired('[', ']');
        if(c == '{')
            return readPaired('{', '}');
        if(c == '@')//annotation
            return '@'+readIdentifier();
        return readSymbol();
    }

    /**
     * Seeks to the start of code and returns everything until the next ';' or '{}' pair
     */
    public String readStatement() {
        seekCode();
        int start = pos;
        char c;
        do {
            c = readElement().charAt(0);
        } while(c != '{' && c != ';');
        return source.substring(start, pos);
    }

    public boolean end() {
        mark();
        seekCode();
        boolean end = pos == source.length();
        reset();
        return end;
    }
}
