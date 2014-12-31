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
    public boolean declaration;

    public SourceReader(String source) {
        this.source = source;
    }

    /**
     * Skips comments and whitespace to find code. Increments pos to point to the next code relevant character (may leave it where it is)
     * @return pos
     */
    public int seekCode() {
        while(pos < source.length()) {
            char c = charAt(pos);
            if (Character.isWhitespace(c))  {
                pos++;
                continue;
            }

            if(c == '/') {
                char c2 = charAt(pos + 1);
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

    public char charAt(int pos) {
        return source.charAt(pos);
    }

    private String readLiteral() {
        int start = pos;
        char c = charAt(pos);
        boolean hex = c == '0' && pos+1 < source.length() && charAt(pos+1) == 'x';
        while(++pos < source.length() && Character.isLetterOrDigit(c = charAt(pos)))
            if(!hex && (c == 'e' || c == 'E')) pos++;//accept the character after an exponent as valid

        return substring(start, pos);
    }

    private String readIdentifier() {
        int start = pos;
        while(++pos < source.length() && Character.isJavaIdentifierPart(charAt(pos)));
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
        while(pos < source.length() && SourceUtil.operator_symbols.contains(substring(start, pos + 1))) pos++;//utilise the fact that all multi-char operators extend a valid operator
        return substring(start, pos);
    }

    private String readString() {
        int start = pos++;
        while(true) {
            char c = charAt(pos++);
            if(c == '\'') pos++;//skip the char after an escape
            else if(c == '"') break;
        }
        return substring(start, pos);
    }

    private String readChar() {
        int start = pos++;//'
        if(charAt(pos) == '\'') pos++;//read escape char
        pos+=2;//read char and '
        return substring(start, pos);
    }

    private String readPaired(char open, char close) {
        int start = pos;
        int level = 0;
        do {
            char c = charAt(pos);
            if(c == '\'') readChar();
            else if(c == '"') readString();
            else {
                pos++;
                if(c == open) level++;
                else if(c == close) level--;
            }
        } while (level > 0);
        return substring(start, pos);
    }

    /**
     * Seeks to the start of code and returns the next element (word, number, bracket/brace pair or valid operator)
     */
    public String readElement() {
        seekCode();
        char c = charAt(pos);
        if(Character.isDigit(c))
            return readLiteral();
        if(Character.isJavaIdentifierStart(c))
            return readIdentifier();
        if(c == '"')
            return readString();
        if(c == '\'')
            return readChar();
        if(c == '(')
            return readPaired('(', ')');
        if(c == '[')
            return readPaired('[', ']');
        if(c == '{')
            return readPaired('{', '}');
        if(declaration && c == '<')
            return readPaired('<', '>');
        if(c == '@')//annotation
            return readIdentifier();
        return readSymbol();
    }

    /**
     * Seeks to the start of code and returns everything until the next ';' or '{}' pair
     */
    public String readStatement() {
        if(end()) return "";

        int start = pos, end;
        char c;
        do {
            c = readElement().charAt(0);
            end = pos;
        } while(!end() && c != '{' && c != ';');

        return substring(start, c == ';' ? end - 1 : end);
    }

    public boolean end() {
        return seekCode() == source.length();
    }

    public boolean isAnnotation() {
        return charAt(seekCode()) == '@' && pos+10 <= source.length() && !substring(pos, pos+10).equals("@interface");
    }

    public void readAnnotations(Scope scope, List<AnnotationSymbol> annotations) {
        while(isAnnotation()) {
            ReferenceSymbol sym = (ReferenceSymbol) scope.resolve1(readElement().substring(1), Symbol.CLASS_SYM);
            String params = charAt(seekCode()) == '(' ? readElement() : "()";
            annotations.add(new AnnotationSymbol(sym));
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

    public String readFullName() {
        String s = readElement();
        while(!end() && charAt(pos) == '.') {
            pos++;
            s = SourceUtil.combineName(s, readElement());
        }
        return s;
    }

    public TypeRef readTypeRef(Scope scope) {
        TypeRef type;
        if (charAt(seekCode()) == '?') {
            readElement();//?
            readElement();//assume extends
            type = new TypeRef(new TypeParam("?", readTypeRef(scope)));
        }
        else {
            type = new TypeRef((TypeSymbol) scope.resolve1(readFullName(), Symbol.TYPE_SYM));
        }

        while(!end() && charAt(pos) == '[') {
            readElement();
            type = new TypeRef(type.type instanceof ConcreteTypeSymbol ? new ConcreteArraySymbol((ConcreteTypeSymbol) type.type) : new ParamaterisedArraySymbol(type.type));
        }

        readTypeRefs(scope, type.params);
        return type;
    }

    public void readTypeRefs(Scope scope, List<TypeRef> typeSymbols) {
        if(end() || charAt(pos) != '<')
            return;

        declaration = true;
        String block = readElement();
        List<String> list = new SourceReader(expand(block)).readList();
        for(String s : list)
            typeSymbols.add(new SourceReader(s).readTypeRef(scope));
        declaration = false;
    }

    /**
     * Assumed block of the form <T extends A>
     */
    public void readTypeParams(Scope scope, List<TypeParam> typeParams) {
        if(end() || charAt(pos) != '<')
            return;

        declaration = true;
        String block = readElement();
        List<String> list = new SourceReader(expand(block)).readList();
        for(String s : list) {
            SourceReader r = new SourceReader(s);
            TypeParam p = new TypeParam(r.readElement());
            typeParams.add(p);
            if(!r.end() && r.readElement().equals("extends"))
                p.upper = r.readTypeRef(scope);
        }
        declaration = false;
    }

    public void skipAnnotations() {
        while(isAnnotation()) {
            readFullName();
            if(charAt(seekCode()) == '(')
                readElement();
        }
    }

    public void skipType() {
        String type = readFullName();
        if (type.charAt(0) == '?') {
            readElement();//assume extends
            skipType();
            return;
        }

        while(!end() && charAt(pos) == '[')
            readElement();

        skipTypeParams();
    }

    public void skipTypeParams() {
        declaration = true;
        if(charAt(seekCode()) == '<')
            readElement();
        declaration = false;
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

    public static String expand(String braced) {
        return braced.substring(1, braced.length()-1);
    }

    private String label;
    /**
     * @return either a Statement, or a List<Statement>
     */
    public Statement readStatement(Scope scope, boolean canDeclare) {
        Statement ret = _readStatement(scope, canDeclare);
        label = null;
        return ret;
    }

    private Statement _readStatement(Scope scope, boolean canDeclare) {
        String s = readStatement();
        if (s.isEmpty()) return new EmptyStatement();
        if (s.charAt(0) == '{') return readBlock(scope, s);

        SourceReader r = new SourceReader(s);
        String elem = r.readElement();
        if (elem.equals("return")) return new ReturnStatement(r.readExpression(scope));
        if (elem.equals("if")) return readIf(scope, r);
        if (elem.equals("while")) return readWhile(scope, r);
        if (elem.equals("do")) return readDo(scope, r);
        if (elem.equals("for")) return readFor(scope, r);
        if (elem.equals("break")) return readBreak(scope, r);
        if (elem.equals("continue")) return readContinue(scope, r);
        if (elem.equals("switch")) return readSwitch(scope, r);
        if (elem.equals("case")) return new SwitchStatement.Case(r.readElement());
        if (elem.equals("default")) return new SwitchStatement.Default();

        String elem2 = r.readElement();
        if (elem2.equals(":")) {
            r.label = elem;
            return r.readStatement(scope, canDeclare);
        }

        if (canDeclare) {
            Statement local = readLocal(scope, s);
            if (local != null)
                return local;
        }

        return new SourceReader(s).readExpression(scope);
    }

    private Statement readSwitch(Scope scope, SourceReader r) {
        SwitchStatement stmt = new SwitchStatement(scope, r.readExpression(scope));
        SourceReader r2 = new SourceReader(expand(r.readElement()));
        while(!r2.end())
            stmt.body.statements.add(r2.readStatement(stmt.body.scope, true));

        return stmt;
    }

    private Statement readLocal(Scope scope, String s) {
        SourceReader r = new SourceReader(s);
        String ident = r.readFullName();
        char c = r.charAt(r.seekCode());
        if(c != '<' && !Character.isJavaIdentifierStart(c))
            return null;

        TypeSymbol type = (TypeSymbol) scope.resolve1(ident, Symbol.TYPE_SYM);
        if(type == null)
            return null;

        TypeRef ref = new TypeRef(type);
        r.readTypeRefs(scope, ref.params);
        List<LocalSymbol> locals = new LinkedList<>();

        for(String svar : r.readList()) {
            SourceReader r2 = new SourceReader(svar);
            LocalSymbol local = new LocalSymbol(ref, r2.readElement());
            if(!r2.end()) {
                r2.readElement();//=
                local.init = r2.readExpression(scope);
            }
            locals.add(local);
            scope.cache(local, local.name);
        }

        if(locals.size() == 1)
            return locals.get(0);

        return new CompactLocalDeclaration(locals);
    }

    private Statement readBreak(Scope scope, SourceReader r) {
        BreakStatement stmt = new BreakStatement();
        if(!r.end())
            stmt.label = (LabelledStatement) scope.resolve1(r.readElement(), Symbol.LABEL);

        return stmt;
    }

    private Statement readContinue(Scope scope, SourceReader r) {
        ContinueStatement stmt = new ContinueStatement();
        if(!r.end())
            stmt.label = (LabelledStatement) scope.resolve1(r.readElement(), Symbol.LABEL);

        return stmt;
    }

    private Statement readFor(Scope scope, SourceReader r) {
        ForStatement stmt = new ForStatement(scope, label);
        SourceReader params = new SourceReader(expand(r.readElement()));
        if(params.indexOf(';') < 0) throw new IllegalArgumentException("foreach loops not supported yet");

        String s_init = params.readStatement();

        Statement locals = readLocal(scope, s_init);
        if(locals != null)
            stmt.init.add(locals);
        else
            for(String s_stmt : new SourceReader(s_init).readList())
                stmt.init.add(new SourceReader(s_stmt).readStatement(scope, false));

        String s_cond = params.readStatement();
        if(s_cond.isEmpty())
            stmt.cond = new Literal("true");
        else
            stmt.cond = new SourceReader(s_cond).readExpression(stmt.scope);

        for(String s_stmt : new SourceReader(params.readStatement()).readList())
            stmt.update.add(new SourceReader(s_stmt).readStatement(stmt.scope, false));

        stmt.body = r.readStatement(stmt.scope, false);

        return stmt;
    }

    private Statement readDo(Scope scope, SourceReader r) {
        DoStatement stmt = new DoStatement(scope, label);
        stmt.body = r.readStatement(stmt.scope, false);
        r.readElement();//while
        stmt.cond = r.readExpression(scope);
        return stmt;
    }

    private Statement readWhile(Scope scope, SourceReader r) {
        WhileStatement stmt = new WhileStatement(scope, label);
        stmt.cond = r.readExpression(scope);
        stmt.body = r.readStatement(stmt.scope, false);
        return stmt;
    }

    private Statement readIf(Scope scope, SourceReader r) {
        IfStatement stmt = new IfStatement(r.readExpression(scope), r.readStatement(scope, false));
        int mark = pos;
        if(!end() && readElement().equals("else"))
            stmt.otherwise = readStatement(scope, false);
        else
            pos = mark;

        return stmt;
    }

    public Expression readExpression(Scope scope) {
        //declaration = true;
        //after reading one element, change decl to false
        seekCode();
        final String s = charAt(pos) == '(' ? readElement() : substring(pos);
        return new Expression()
        {
            @Override
            public TypeRef returnType() {
                return null;
            }

            @Override
            public int precedence() {
                return 0;
            }

            @Override
            public String toString() {
                return s;
            }
        };
    }

    private Block readBlock(Scope scope, String s) {
        Block block = new Block(scope);
        SourceReader r = new SourceReader(expand(s));
        while(!r.end())
            block.statements.add(r.readStatement(block.scope, true));

        block.index();
        return block;
    }
}
