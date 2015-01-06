package gpuproj.translator;

import gpuproj.srctree.*;
import gpuproj.srctree.Scope.ScopeProvider;
import gpuproj.translator.OCLProgramBuilder.Declaration;
import gpuproj.translator.OCLProgramBuilder.Implementation;

import java.lang.reflect.Modifier;
import java.util.*;

public class JavaTranslator implements ScopeProvider
{
    public class ConstantDecl implements Declaration
    {
        public FieldSymbol field;

        public ConstantDecl(FieldSymbol field) {
            this.field = field;
            field.type.modifiers |= TypeRef.CONSTANT;
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public String declare() {
            StringBuilder sb = new StringBuilder();
            TypeRef.printCL = true;
            sb.append(field.type).append(' ').append(field.getName()).append(" = ").append(field.init).append(';');
            TypeRef.printCL = false;
            return sb.toString();
        }
    }

    public class MethodDecl implements Declaration, Implementation
    {
        public MethodSymbol method;

        public MethodDecl(MethodSymbol method) {
            this.method = method;
        }

        @Override
        public String getName() {
            return method.getName();
        }

        private void declare(StringBuilder sb) {
            sb.append(method.returnType).append(' ').append(method.getName());
            sb.append('(').append(SourceUtil.listString(method.params)).append(')');
        }

        @Override
        public String declare() {
            StringBuilder sb = new StringBuilder();
            TypeRef.printCL = true;
            declare(sb);
            sb.append(';');
            TypeRef.printCL = false;
            return sb.toString();
        }

        @Override
        public String implement() {
            StringBuilder sb = new StringBuilder();
            TypeRef.printCL = true;
            declare(sb);
            sb.append(' ').append(method.body);
            TypeRef.printCL = false;
            return sb.toString();
        }
    }

    public class StructDecl implements Declaration
    {
        public ClassSymbol type;
        public List<FieldSymbol> fields = new LinkedList<FieldSymbol>();

        public StructDecl(ClassSymbol type) {
            this.type = type;
            TranslatedStruct.getStructFields(type, fields);
        }

        @Override
        public String getName() {
            return type.getName();
        }

        @Override
        public String declare() {
            StringBuilder sb = new StringBuilder();
            sb.append("typedef struct {\n");

            TypeRef.printCL = true;
            for(FieldSymbol field : fields)
                sb.append("   ").append(field.type).append(' ').append(field.getName()).append(";\n");
            TypeRef.printCL = false;

            sb.append("} ").append(type.getName()).append(';');
            return sb.toString();
        }
    }

    /**
     * A replacement for program scope variables, these are declared in the kernel and passed as extra function arguments to methods that need them.
     */
    public class KernelVar
    {

    }

    public class MethodInfo
    {
        public Set<MethodSymbol> callers = new HashSet<MethodSymbol>();
        public Set<MethodSymbol> calls = new HashSet<MethodSymbol>();
        public Set<KernelVar> globals = new HashSet<KernelVar>();
    }

    private class ExpansionVisitor extends StatementVisitor
    {
        private Scope scope;

        @Override
        public void visit(Block block) {
            Scope outer = scope;
            scope = block.scope;
            super.visit(block);
            scope = outer;
        }

        @Override
        public void visit(MethodCall exp) {
            super.visit(exp);

            if(!exp.method.isStatic()) {//find the correct instance implementation
                ClassSymbol type = exp.params.get(0).returnType().classType();
                List<MethodSymbol> methods = type.getMethods(exp.method.getName());
                exp.method = MethodSymbol.match(methods, exp.params.subList(1, exp.params.size()));
            }

            exp.method = addGlobalMethod(exp.method, exp.params);

            for(Iterator<Expression> it = exp.params.iterator(); it.hasNext();)
                if(isGlobalType(it.next()))
                    it.remove();
        }

        @Override
        public void visit(VariableAccess exp) {
            if(exp.var instanceof FieldSymbol) {
                FieldSymbol field = (FieldSymbol) exp.var;
                if(field.isStatic())
                    exp.var = addGlobalField(field);
                else if(exp.exp instanceof This) {
                    if(isGlobalType(exp.exp)) {
                        exp.exp = null;
                        exp.var = addGlobalField(field);
                    } else {
                        exp.exp = new VariableAccess((LocalSymbol) scope.resolve1("this", Symbol.LOCAL_SYM));
                    }
                }
            }

            super.visit(exp);
        }

        @Override
        public void visit(BinaryOp exp) {
            super.visit(exp);
            if(exp.op.equals(">>>")) {
                exp.op = ">>";
                if(exp.op1.precedence() >= 3)
                    exp.op1 = new Parentheses(exp.op1);
                exp.op1 = new Cast(exp.op1.returnType().copy().modify(TypeRef.UNSIGNED), exp.op1);
            } else if(exp.op.endsWith("=")) {
                if(exp.op1 instanceof VariableAccess) {
                    VariableAccess varAcc = (VariableAccess)exp.op1;
                    if(varAcc.var instanceof FieldSymbol)
                        ((FieldSymbol) varAcc.var).modifiers &= ~Modifier.FINAL;
                }
            }
        }
    }

    private class MethodTranslator extends StatementVisitor
    {
        public MethodSymbol method;

        public MethodTranslator(MethodSymbol method) {
            this.method = method;
        }

        public void translate() {
            for(Iterator<LocalSymbol> it = method.params.iterator(); it.hasNext();)
                if(isGlobalType(it.next()))
                    it.remove();

            new ExpansionVisitor().visit(method.body);

            for(LocalSymbol local : method.params)
                if(local.type.pointer == 0 && local.type.type instanceof ClassSymbol)
                    local.type.pointer++;
        }
    }

    public OCLProgramBuilder program;
    /**
     * Object used as MethodSymbol.source for builtin OpenCL methods
     */
    public static final Object builtinSource = new Object();
    private Scope scope = new Scope(null, this);
    private Map<MethodSymbol, MethodSymbol> translatedMap = new HashMap<MethodSymbol, MethodSymbol>();
    private Map<MethodSymbol, MethodInfo> methodInfo = new HashMap<MethodSymbol, MethodInfo>();
    private Set<ClassSymbol> globalClasses = new HashSet<ClassSymbol>();
    private Set<ClassSymbol> structs = new HashSet<ClassSymbol>();
    private Map<FieldSymbol, FieldSymbol> globalFields = new HashMap<FieldSymbol, FieldSymbol>();
    private List<FieldSymbol> fieldOrder = new LinkedList<FieldSymbol>();

    public JavaTranslator(OCLProgramBuilder program) {
        this.program = program;
    }

    public void addStruct(final ClassSymbol sym) {
        if(structs.contains(sym))
            return;

        if(program.getDeclaration(sym.getName()) != null)
            throw new UnsupportedOperationException(sym.getName()+" already declared"); //could implement some remapping system

        program.declare(new StructDecl(sym));
    }

    /**
     * Designates a type as global to the scope of the program being built.
     * Referenced instance fields will be converted to KernelVars
     * Referenced instance methods will be made global
     * Any method parameters with this type will be removed
     * This method should be called before any references to this type are made as only new methods will be transformed
     */
    public void addGlobalInstance(ClassSymbol sym) {
        globalClasses.add(sym);
    }

    public FieldSymbol addGlobalField(FieldSymbol sym) {
        FieldSymbol translated = globalFields.get(sym);
        if(translated != null)
            return translated;

        if(program.getDeclaration(sym.getName()) != null)
            throw new UnsupportedOperationException(sym.getName()+" already declared"); //could implement some remapping system

        translated = new FieldSymbol(sym.getName());
        translated.modifiers |= Modifier.FINAL | Modifier.STATIC;
        translated.type = sym.type;
        globalFields.put(sym, translated);

        if(sym.init == null) sym.loadInitialiser();
        translated.init = sym.init.copy(scope);

        new ExpansionVisitor().visit(translated.init);

        fieldOrder.add(translated);

        return translated;
    }

    public MethodSymbol addGlobalMethod(MethodSymbol sym, List callParams) {
        MethodSymbol translated = translatedMap.get(sym);
        if(translated != null) {
            if(canCall(translated, callParams))
                return translated;
            throw new IllegalArgumentException("Cannot call flattened method "+translated+" with params ("+SourceUtil.listString(callParams)+")");
        }

        String builtin = BuiltinMethodMap.map(sym);
        if(builtin != null) {
            translated = new MethodSymbol(builtin, scope, builtinSource);
            translated.modifiers = Modifier.STATIC;
            translated.returnType = sym.returnType;
            translated.params = sym.params;
            translatedMap.put(sym, translated);
            return translated;
        }

        if(isOCLStatic(sym)) {
            OCLStaticConverter c;
            try {
                c = sym.getAnnotation(OCLStatic.class).value().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            translated = c.convert(sym, this);
            translatedMap.put(sym, translated);
            return translated;
        }

        if(program.getDeclaration(sym.getName()) != null)
            throw new UnsupportedOperationException(sym.getName()+" already declared"); //could implement some remapping system

        translated = new MethodSymbol(sym.getName(), scope, this);
        translated.modifiers = Modifier.STATIC;

        TypeRef t = sym.returnType;//infer a concrete return type from params
        Map<TypeParam, TypeSymbol> typeMap = TypeRef.specify(t.getParams(), sym.params, callParams);
        translated.returnType = new TypeRef(t.mapParams(typeMap).concrete());

        int i = 0;
        if(!sym.isStatic())
            translated.params.add(new LocalSymbol(TypeRef.get(callParams.get(i++)).copy(), "this"));

        for(int j = 0; i < callParams.size(); i++, j++)
            translated.params.add(new LocalSymbol(TypeRef.get(callParams.get(i)).copy(), sym.params.get(j).name));

        if(sym.body == null) sym.loadBody();
        translated.body = sym.body.copy(translated.scope);

        translatedMap.put(sym, translated);
        new MethodTranslator(translated).translate();
        MethodDecl w = new MethodDecl(translated);
        program.declare(w);
        program.implement(w);
        return translated;
    }

    private boolean canCall(MethodSymbol sym, List params) {
        if(sym.source == builtinSource)
            return true;

        for(int i = 0, j = 0; i < params.size();) {
            TypeRef p = TypeRef.get(params.get(i++));
            if(isGlobalType(p))//will be discarded
                continue;

            TypeSymbol type = sym.params.get(j++).type.type;
            if (type instanceof ClassSymbol && p.type != type)
                return false;
        }

        return true;
    }

    private boolean isGlobalType(Object o) {
        return globalClasses.contains(TypeRef.get(o).type);
    }

    /**
     * Returns a call string for calling a global function from the kernel. It will automatically fill in the kernel scope parameters as arguments
     * @param method The method to call
     * @param args Non-global
     * @return Valid kernel function code for the method call
     */
    public String kernelCall(MethodSymbol method, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append('(');
        int i = 0;
        for(String arg : args) {
            if (i++ > 0) sb.append(", ");
            sb.append(arg);
        }

        //TODO append kernel args

        return sb.append(')').toString();
    }

    public void translate() {
        for(FieldSymbol field : fieldOrder)
            if((field.modifiers & Modifier.FINAL) != 0)
                program.declare(new ConstantDecl(field));
            //TODO else add kernel arg
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {

    }

    @Override
    public Scope scope() {
        return scope;
    }

    public static boolean isOCLStatic(MethodSymbol sym) {
        return sym.getAnnotation(OCLStatic.class) != null;
    }
}
