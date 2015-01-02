package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.List;

public class SourceClassSymbol extends ClassSymbol
{
    public SourceClassSymbol(String fullname, Scope scope, String source) {
        super(fullname, scope, source);
    }

    @Override
    public ClassSymbol loadAnnotations() {
        new SourceReader((String) source).readAnnotations(scope, annotations);

        for (ClassSymbol inner : innerClasses)
            inner.loadAnnotations();

        for (FieldSymbol f : fields)
            new SourceReader((String) f.source).readAnnotations(scope, f.annotations);

        for (MethodSymbol m : methods)
            new SourceReader((String) m.source).readAnnotations(scope, m.annotations);

        return this;
    }

    @Override
    public SourceClassSymbol loadSignatures() {
        SourceReader r = new SourceReader((String) source);
        r.skipAnnotations();
        r.seek("interface", "class");
        r.readElement();//skip interface/class
        r.readElement();//skip name
        r.readTypeParams(this);

        String word = r.readElement();
        if(isInterface());//interfaces have no superclass
        else if(word.equals("extends")) {
            parent = r.readTypeRef(scope);
            word = r.readElement();
        } else
            parent = new TypeRef(TypeIndex.instance().OBJECT);

        if(word.equals("implements") || word.equals("extends") && isInterface()) {
            int start = r.seekCode();
            int end = r.indexOf('{');
            List<String> list = new SourceReader(r.substring(start, end)).readList();
            for(String s : list)
                interfaces.add(new SourceReader(s).readTypeRef(scope));
            r.pos = end;
        }

        for (ClassSymbol inner : innerClasses)
            inner.loadSignatures();

        for (FieldSymbol f : fields)
            loadSignature(f);

        for (MethodSymbol m : methods)
            loadSignature(m);

        return this;
    }

    private void loadSignature(MethodSymbol m) {
        SourceReader r = new SourceReader((String) m.source);
        r.skipAnnotations();
        m.modifiers = r.readModifiers();
        r.readTypeParams(m);
        if(m.getName().equals("<init>"))
            m.returnType = new TypeRef(this);
        else
            m.returnType = r.readTypeRef(m.scope);
        r.readElement();//method name
        String s_params = r.readElement();
        List<String> params = new SourceReader(SourceReader.expand(s_params)).readList();
        for(String s : params) {
            if(s.contains("...")) {
                s = s.replace("...", "[]");
                m.vaargs = true;
            }
            SourceReader r2 = new SourceReader(s);
            m.params.add(new LocalSymbol(r2.readTypeRef(m.scope), r2.readElement()));
        }
    }

    private void loadSignature(FieldSymbol f) {
        SourceReader r = new SourceReader((String) f.source);
        r.skipAnnotations();
        f.modifiers = r.readModifiers();
        f.type = r.readTypeRef(scope);
    }

    @Override
    public SourceClassSymbol loadSymbols() {
        SourceReader r = new SourceReader((String) source);
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
        String body = r.readElement();
        r = new SourceReader(SourceReader.expand(body));
        while(!r.end()) {
            String stmt = r.readStatement();
            if(stmt.isEmpty()) continue;
            switch (declarationType(stmt)) {
                case CLASS_SYM:
                    innerClasses.add(fromStatement(fullname, scope, stmt).loadSymbols());
                    break;
                case FIELD_SYM:
                    fields.add(newField(stmt));
                    break;
                case METHOD_SYM:
                    methods.add(newMethod(stmt));
                    break;
            }
        }

        return this;
    }

    private MethodSymbol newMethod(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        r.seek("(");
        while(Character.isWhitespace(r.charAt(--r.pos)));//rollback
        while(Character.isJavaIdentifierPart(r.charAt(--r.pos)));
        String name = r.readElement();
        if(name.equals(getName()))
            name = "<init>";
        return new MethodSymbol(SourceUtil.combineName(fullname, name), this, stmt);
    }

    private FieldSymbol newField(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        r.readModifiers();
        r.skipType();//type
        return new FieldSymbol(SourceUtil.combineName(fullname, r.readElement()), this, stmt);
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
        if(bracket < 0) bracket = Integer.MAX_VALUE;
        if(clazz < 0) clazz = Integer.MAX_VALUE;

        if(clazz < equals && clazz < bracket) return CLASS_SYM;
        if(bracket < equals || brace < equals) return METHOD_SYM;
        return FIELD_SYM;
    }

    public static SourceClassSymbol fromStatement(String parent, Scope scope, String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        r.seek("interface", "class");
        r.readElement();//skip interface/class
        return new SourceClassSymbol(SourceUtil.combineName(parent, r.readElement()), scope, stmt);
    }
}
