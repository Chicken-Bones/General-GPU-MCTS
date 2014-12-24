package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ClassSymbol extends ReferenceSymbol
{
    public List<AnnotationSymbol> annotations = new LinkedList<>();

    public ClassSymbol(String fullname, Scope scope, String source) {
        super(fullname, scope, source);
    }

    @Override
    public ClassSymbol loadSignatures() {
        SourceReader r = new SourceReader((String) source);
        r.skipAnnotations();
        r.seek("interface", "class");
        r.readElement();//skip interface/class
        r.readElement();//skip name
        r.readTypeParams(scope, typeParams);

        String word = r.readElement();
        if(word.equals("extends")) {
            parent = r.readTypeRef(scope);
            word = r.readElement();
        } else if(!fullname.equals("java.lang.Object")) {
            parent = new TypeRef((TypeSymbol) scope.resolve1("java.lang.Object", CLASS_SYM));
        }

        if(word.equals("implements")) {
            int start = r.seekCode();
            int end = r.indexOf('{');
            List<String> list = new SourceReader(r.substring(start, end)).readList();
            for(String s : list)
                interfaces.add(new SourceReader(s).readTypeRef(scope));
            r.pos = end;
        }

        for (ReferenceSymbol inner : innerClasses)
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
        r.readTypeParams(scope, m.typeParams);
        m.returnType = r.readTypeRef(m.scope);
        r.readElement();//method name
        String s_params = r.readElement();
        List<String> params = new SourceReader(s_params.substring(1, s_params.length()-1)).readList();
        for(String s : params) {
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
    public ClassSymbol loadSymbols() {
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
        r = new SourceReader(body.substring(1, body.length()-1));
        while(!r.end()) {
            String stmt = r.readStatement();
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
        r.readModifiers();
        r.skipTypeParams();//method type aliases
        r.readElement();//return type
        r.skipTypeParams();//return type params
        //TODO detect constructors
        return new MethodSymbol(SourceUtil.combineName(fullname, r.readElement()), this, stmt);
    }

    private FieldSymbol newField(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.skipAnnotations();
        r.readModifiers();
        r.readElement();//type
        r.skipTypeParams();
        return new FieldSymbol(SourceUtil.combineName(fullname, r.readElement()), stmt);
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

    public static ClassSymbol fromStatement(String parent, Scope scope, String stmt) {
        SourceReader r = new SourceReader(stmt);
        int i = r.indexOf("class");
        if(i < 0) i = r.indexOf("interface");
        r.pos = i;
        r.readElement();
        return new ClassSymbol(SourceUtil.combineName(parent, r.readElement()), scope, stmt);
    }
}
