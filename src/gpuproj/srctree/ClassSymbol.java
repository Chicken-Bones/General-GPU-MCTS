package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ClassSymbol extends TypeSymbol implements ScopeProvider
{
    public static final int ANNOTATION = 0x00002000;
    public static final int ENUM = 0x00004000;

    public final String declaration;
    public final Scope scope;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<>();
    public ClassSymbol parent;
    public List<ClassSymbol> interfaces = new LinkedList<>();
    public List<ClassSymbol> innerClasses = new LinkedList<>();
    public List<FieldSymbol> fields = new LinkedList<>();
    public List<MethodSymbol> methods = new LinkedList<>();
    public List<AnnotationSymbol> annotations = new LinkedList<>();

    public ClassSymbol(String fullname, String declaration, Scope scope) {
        super(fullname);
        this.declaration = declaration;
        this.scope = new Scope(scope, this);
        readSymbols();
        TypeIndex.instance.register(this);
        readSignatures();
        //readAnnotations();
    }

    private void readSignatures() {
        SourceReader r = new SourceReader(declaration);
        r.skipAnnotations();
        r.seek("interface", "class");
        r.readElement();//skip name
        r.readTypeParams(scope, typeParams);

        String word = r.readElement();
        if(word.equals("extends")) {
            parent = (ClassSymbol) scope.resolve(r.readElement(), CLASS_SYM);
            word = r.readElement();
        } else if(!fullname.equals("java.lang.Object")) {
            parent = (ClassSymbol) scope.resolve("java.lang.Object", CLASS_SYM);
        }

        if(word.equals("implements")) {
            int start = r.seekCode();
            int end = r.indexOf('{');
            List<String> list = new SourceReader(r.substring(start, end)).readList();
            for(String s : list)
                interfaces.add((ClassSymbol) scope.resolve(new SourceReader(s).readElement(), CLASS_SYM));
            r.pos = end;
        }

        //todo
    }

    private void readSymbols() {
        SourceReader r = new SourceReader(declaration);
        r.skipAnnotations();
        modifiers = r.readModifiers();

        switch(r.readElement()) {
            case "@interface":
                modifiers |= ANNOTATION;
                break;
            case "interface":
                modifiers |= Modifier.INTERFACE;
                break;
            case "enum":
                modifiers |= ENUM;
                break;
            case "class":
                break;
            default:
                throw new IllegalStateException("Unknown class type");
        }

        //skip to body
        r.seek("{");
        readBodySymbols(r.readElement());
    }

    private void readBodySymbols(String body) {
        SourceReader r = new SourceReader(body.substring(1, body.length()-1));
        while(!r.end()) {
            String stmt = r.readStatement();
            switch (declarationType(stmt)) {
                case CLASS_SYM:
                    innerClasses.add(fromStatement(scope, fullname, stmt));
                    break;
                case FIELD_SYM:
                    fields.add(FieldSymbol.fromStatement(fullname, stmt));
                    break;
                case METHOD_SYM:
                    methods.add(MethodSymbol.fromStatement(fullname, stmt));
                    break;
            }
        }
    }

    private int declarationType(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        int equals = r.indexOf('=');
        int brace = r.indexOf('{');
        int bracket = r.indexOf('(');
        int clazz = r.indexOf("class");

        if(equals < 0) equals = Integer.MAX_VALUE;
        if(brace < 0) brace = Integer.MAX_VALUE;
        if(bracket < 0) equals = Integer.MAX_VALUE;
        if(clazz < 0) clazz = Integer.MAX_VALUE;

        if(clazz < equals && clazz < bracket) return CLASS_SYM;
        if(bracket < equals || brace < equals) return METHOD_SYM;
        return FIELD_SYM;
    }

    @Override
    public Symbol resolveSingle(String name, int type) {
        if((type & FIELD_SYM) != 0) {
            for(FieldSymbol sym : fields)
                if(sym.name.equals(name))
                    return sym;
        }
        if((type & METHOD_SYM) != 0) {
            for(MethodSymbol sym : methods)
                if(sym.name.equals(name))
                    return sym;
        }
        if((type & CLASS_SYM) != 0) {
            for (ClassSymbol sym : innerClasses)
                if (sym.name.equals(name))
                    return sym;
        }
        if((type & TYPEPARAM) != 0) {
            for(TypeParam p : typeParams)
                if(p.name.equals(name))
                    return p;
        }
        if(parent != null) {
            Symbol sym = parent.resolveSingle(name, type);
            if(sym != null) return sym;
        }
        for(ClassSymbol iface : interfaces) {
            Symbol sym = iface.resolveSingle(name, type);
            if(sym != null) return sym;
        }
        return null;
    }

    public FieldSymbol resolveField(String name) {
        return (FieldSymbol) resolveSingle(name, FIELD_SYM);
    }

    @Override
    public TypeSymbol concrete() {
        return this;
    }

    public static ClassSymbol fromStatement(Scope scope, String parent, String stmt) {
        SourceReader r = new SourceReader(stmt);
        int i = r.indexOf("class");
        if(i < 0) i = r.indexOf("interface");
        r.pos = i;
        r.readElement();
        return new ClassSymbol(SourceUtil.combineName(parent, r.readElement()), stmt, scope);
    }
}
