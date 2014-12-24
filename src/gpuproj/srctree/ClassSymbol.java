package gpuproj.srctree;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ClassSymbol extends ReferenceSymbol
{
    public List<AnnotationSymbol> annotations = new LinkedList<>();

    public ClassSymbol(String fullname, Scope scope, String source) {
        super(fullname, scope, source);
        //readAnnotations();
    }

    @Override
    public void loadSignatures() {
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
            parent = new TypeRef((TypeSymbol) scope.resolve("java.lang.Object", CLASS_SYM));
        }

        if(word.equals("implements")) {
            int start = r.seekCode();
            int end = r.indexOf('{');
            List<String> list = new SourceReader(r.substring(start, end)).readList();
            for(String s : list)
                interfaces.add(new SourceReader(s).readTypeRef(scope));
            r.pos = end;
        }

        //todo
    }

    @Override
    public void loadSymbols() {
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
                    innerClasses.add(fromStatement(fullname, scope, stmt));
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

    public static ClassSymbol fromStatement(String parent, Scope scope, String stmt) {
        SourceReader r = new SourceReader(stmt);
        int i = r.indexOf("class");
        if(i < 0) i = r.indexOf("interface");
        r.pos = i;
        r.readElement();
        return new ClassSymbol(SourceUtil.combineName(parent, r.readElement()), scope, stmt);
    }
}
