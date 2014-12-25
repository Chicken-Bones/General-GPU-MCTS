package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

public class SourceReader
{
    public final String source;
    public int pos;
    /**
     * Flag used to determine if < is less than, or type parameter. Should be changed by the reader operator
     */
    public boolean declaration = true;

    public SourceReader(String source) {
        this.source = source;
    }

    /**
     * Skips comments and whitespace to find code. Increments pos to point to the next code relevant character (may leave it where it is)
     * @return pos
     */
    public int seekCode() {
        while(pos < source.length()) {
            char c = source.charAt(pos);
            if (Character.isWhitespace(c))  {
                pos++;
                continue;
            }

            if(c == '/') {
                char c2 = source.charAt(pos+1);
                if(c2 == '/') {//line comment
                    int idx = indexOf('\n');
                    if(idx < 0) pos = source.length();
                    else pos = idx+1;
                } else if(c2 == '*') {//block comment
                    int idx = indexOf("*/");
                    if (idx < 0) pos = source.length();
                    else pos = idx + 2;
                }
                continue;
            }

            break;
        }
        return pos;
    }

    public int indexOf(char c) {
        return source.indexOf(c, pos);
    }

    public int indexOf(String s) {
        return source.indexOf(s, pos);
    }

    public String substring(int start) {
        return source.substring(start);
    }

    public String substring(int start, int end) {
        return source.substring(start, end);
    }

    private String readLiteral() {
        int start = pos;
        char c = source.charAt(pos);
        boolean hex = c == '0' && source.charAt(pos+1) == 'x';
        while(pos < source.length() && Character.isLetterOrDigit(c = source.charAt(++pos)))
            if(!hex && (c == 'e' || c == 'E')) pos++;//accept the character after an exponent as valid

        return substring(start, pos);
    }

    private String readIdentifier() {
        int start = pos;
        while(++pos < source.length() && Character.isJavaIdentifierPart(source.charAt(pos)));
        return substring(start, pos);
    }

    /**
     * Does not seek
     */
    public String readTo(char delim, boolean include) {
        int start = pos;
        pos = indexOf(';')+1;
        return substring(start, include ? pos : pos-1);
    }

    private String readSymbol() {
        int start = pos++;
        while(SourceUtil.operator_symbols.contains(substring(start, pos + 1))) pos++;//utilise the fact that all multi-char operators extend a valid operator
        return substring(start, pos);
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
        return substring(start, pos);
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
        if(declaration && c == '<')
            return readPaired('<', '>');
        if(c == '@')//annotation
            return '@'+readIdentifier();
        return readSymbol();
    }

    /**
     * Seeks to the start of code and returns everything until the next ';' or '{}' pair
     */
    public String readStatement() {
        int start = seekCode();
        char c;
        do {
            c = readElement().charAt(0);
        } while(c != '{' && c != ';');

        if(c == ';' && start == pos-1)//redundant semicolon, or semicolon after block
            return readStatement();

        return substring(start, c == ';' ? pos - 1 : pos);
    }

    public boolean end() {
        return seekCode() == source.length();
    }

    public boolean isAnnotation() {
        return source.charAt(seekCode()) == '@' && pos+10 <= source.length() && !substring(pos, pos+10).equals("@interface");
    }

    public void readAnnotations(Scope scope, List<AnnotationSymbol> annotations) {
        while(isAnnotation()) {
            ClassSymbol sym = (ClassSymbol) scope.resolve1(readElement().substring(1), Symbol.CLASS_SYM);
            String params = source.charAt(seekCode()) == '(' ? readElement() : "()";
            annotations.add(new AnnotationSymbol(sym, params));
        }
    }

    /**
     * Reads a comma separated list. Stops at the end of the source reader
     */
    public List<String> readList() {
        List<String> list = new LinkedList<>();
        int start = seekCode();
        int end = start;
        while(!end()) {
            String w = readElement();
            if(w.equals(",")) {
                list.add(substring(start, end));
                start = seekCode();
            } else
                end = pos;
        }
        if(start != end)
            list.add(substring(start, end));//last element

        return list;
    }

    public int readModifiers() {
        int mark = seekCode();
        int mods = 0;
        while(true) {
            int mod = SourceUtil.getModifier(readElement());
            if(mod == 0) {
                pos = mark;
                return mods;
            }
            mods |= mod;
            mark = pos;
        }
    }

    public TypeRef readTypeRef(Scope scope) {
        TypeRef type;
        if (source.charAt(seekCode()) == '?') {
            readElement();//?
            readElement();//assume extends
            type = new TypeRef(new TypeParam("?", readTypeRef(scope)));
        }
        else {
            type = new TypeRef((TypeSymbol) scope.resolve1(readElement(), Symbol.TYPE_SYM));
        }

        while(!end() && source.charAt(pos) == '[') {
            readElement();
            type = new TypeRef(new ArrayTypeSymbol(type.type));
        }

        readTypeRefs(scope, type.params);
        return type;
    }

    public void readTypeRefs(Scope scope, List<TypeRef> typeSymbols) {
        if(end() || source.charAt(pos) != '<')
            return;

        String block = readElement();
        List<String> list = new SourceReader(block.substring(1, block.length()-1)).readList();
        for(String s : list)
            typeSymbols.add(new SourceReader(s).readTypeRef(scope));
    }

    /**
     * Assumed block of the form <T extends A>
     */
    public void readTypeParams(Scope scope, List<TypeParam> typeParams) {
        if(end() || source.charAt(pos) != '<')
            return;

        String block = readElement();
        List<String> list = new SourceReader(block.substring(1, block.length()-1)).readList();
        for(String s : list) {
            SourceReader r = new SourceReader(s);
            String alias = r.readElement();
            TypeParam p = null;
            if(!r.end()) {
                String boundKey = r.readElement();
                TypeRef bound = r.readTypeRef(scope);
                if(boundKey.equals("extends"))
                    p = new TypeParam(alias, bound);
            }
            if(p == null)
                p = new TypeParam(alias);

            typeParams.add(p);
        }
    }

    public void skipAnnotations() {
        while(isAnnotation()) {
            readElement();
            if(source.charAt(seekCode()) == '(')
                readElement();
        }
    }

    public void skipType() {
        String type = readElement();
        if (type.charAt(0) == '?') {
            readElement();//assume extends
            skipType();
            return;
        }

        while(!end() && source.charAt(pos) == '[')
            readElement();

        skipTypeParams();
    }

    public void skipTypeParams() {
        declaration = true;
        if(source.charAt(seekCode()) == '<')
            readElement();
    }

    public void seek(String... needles) {
        int first = Integer.MAX_VALUE;
        for(String needle : needles) {
            int p = indexOf(needle);
            if(p >= 0 && p < first) first = p;
        }
        if(first < Integer.MAX_VALUE)
            pos = first;
    }
}
