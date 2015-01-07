package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.List;

public class SourceClassSymbol extends ClassSymbol
{
    public SourceClassSymbol(String fullname, Scope scope, String source) {
        super(fullname, scope, source);
    }

    @Override
    public SourceClassSymbol loadSignatures() {
        SourceReader r = new SourceReader((String) source);
        r.seekStart("interface", "class");
        r.readElement();//skip interface/class
        r.readElement();//skip name
        r.readTypeParams(this);

        String word = r.readElement();
        if(isInterface());//interfaces have no superclass
        else if(word.equals("extends")) {
            parent = r.readTypeRef(scope);
            word = r.readElement();
        } else
            parent = new TypeRef(TypeIndex.OBJECT);

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
        f.modifiers = r.readModifiers();
        f.type = r.readTypeRef(scope);
    }

    @Override
    public SourceClassSymbol loadSymbols() {
        SourceReader r = new SourceReader((String) source);
        modifiers = r.readModifiers();

        String s = r.readElement();
        if (s.equals("@interface"))
            modifiers |= ANNOTATION;
        else if (s.equals("interface"))
            modifiers |= Modifier.INTERFACE;
        else if (s.equals("enum"))
            modifiers |= ENUM;
        else if (!s.equals("class"))
            throw new IllegalStateException("Unknown class type");

        //skip to body
        r.seekStart("{");
        String body = r.readElement();
        r = new SourceReader(SourceReader.expand(body));
        while(!r.end()) {
            String stmt = r.readStatement();
            if(stmt.isEmpty()) continue;
            switch (declarationType(stmt)) {
                case CLASS_SYM:
                    innerClasses.add(fromStatement(fullname, scope, stmt).loadSymbols().setInner());
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
        r.seekStart("(");
        r.rollbackIdentifier();
        String name = r.readElement();
        if(name.equals(getName()))
            name = "<init>";
        return new MethodSymbol(SourceUtil.combineName(fullname, name), this, stmt);
    }

    private FieldSymbol newField(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.readModifiers();
        r.skipType();//type
        return new FieldSymbol(SourceUtil.combineName(fullname, r.readElement()), this, stmt);
    }

    private int declarationType(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.readModifiers();
        String s = r.readElement();
        if(s.equals("class") || s.equals("interface") || s.equals("@interface") || s.equals("enum"))
            return CLASS_SYM;

        return SourceUtil.methodOrField(stmt);
    }

    public static SourceClassSymbol fromStatement(String parent, Scope scope, String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.readModifiers();
        r.readElement();//skip interface/class
        return new SourceClassSymbol(SourceUtil.combineName(parent, r.readElement()), scope, stmt);
    }
}
