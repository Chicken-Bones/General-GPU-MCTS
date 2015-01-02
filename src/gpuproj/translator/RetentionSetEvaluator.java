package gpuproj.translator;

import gpuproj.srctree.*;

import java.util.*;

/**
 * Recursively calcualtes the retained symbol set as follows
 * 1. All methods and fields referenced by all statements
 * 2. Classes (structs) referenced by new
 * 3. Fields of retained classes and their types
 * 4. Superclasses of retained classes
 */
public class RetentionSetEvaluator extends StatementVisitor
{
    public Set<Symbol> retained = new HashSet<>();
    public Set<Symbol> search = new HashSet<>();
    public Set<Symbol> handled = new HashSet<>();

    public List<MethodRemapper> remappers = new LinkedList<>();

    public RetentionSetEvaluator() {
        remappers.add(BuiltinMethodRemapper.instance);
        remappers.add(DefaultMethodRemapper.instance);
    }

    public void add(Symbol sym) {
        if(!retained.contains(sym) && !handled.contains(sym))
            search.add(sym);
    }

    public void search() {
        while(!search.isEmpty()) {
            Symbol sym = search.iterator().next();

            if(handle(sym))
                handled.add(sym);
            else {
                visit(sym);
                retained.add(sym);
            }

            search.remove(sym);
        }
    }

    public static boolean isOCLGlobal(ClassSymbol sym) {
        for(AnnotationSymbol ann : sym.annotations)
            if(ann.type.fullname.equals("gpuproj.translator.OCLGlobal"))
                return true;

        return false;
    }

    public static  boolean isOCLStatic(MethodSymbol sym) {
        for(AnnotationSymbol ann : sym.annotations)
            if(ann.type.fullname.equals("gpuproj.translator.OCLStatic"))
                return true;

        return false;
    }

    public boolean handle(Symbol sym) {
        if(sym instanceof MethodSymbol)
            return handle((MethodSymbol) sym);
        if(sym instanceof FieldSymbol)
            return handle((FieldSymbol) sym);
        if(sym instanceof ClassSymbol)
            return handle((ClassSymbol) sym);

        return false;
    }

    public boolean handle(ClassSymbol sym) {
        return isOCLGlobal(sym);
    }

    public boolean handle(MethodSymbol sym) {
        return sym.source == BuiltinMethodRemapper.instance || isOCLStatic(sym) || isOCLGlobal(sym.owner());
    }

    public boolean handle(FieldSymbol sym) {
        return false;
    }

    public void visit(Symbol sym) {
        if(sym instanceof MethodSymbol)
            visit((MethodSymbol) sym);
        else if(sym instanceof FieldSymbol)
            visit((FieldSymbol) sym);
        else if(sym instanceof ClassSymbol)
            visit((ClassSymbol) sym);
    }

    public void visit(MethodSymbol sym) {
        if(sym.body == null)
            sym.loadBody();

        visit(sym.body);
    }

    public void visit(FieldSymbol sym) {
        if(sym.init == null)
            sym.loadInitialiser();

        visit(sym.init);
    }

    public void visit(ClassSymbol sym) {
        if(isOCLGlobal(sym))
            return;

        TypeSymbol parent = sym.parent.concrete();
        if(!parent.fullname.equals("java.lang.Object"))
            add(parent);

        for(FieldSymbol field : sym.fields)
            if(!field.isStatic()) {
                add(field);
                add(field.type.concrete());
            }
    }

    @Override
    public void visit(VariableAccess exp) {
        if(exp.var instanceof FieldSymbol)
            add(exp.var);

        super.visit(exp);
    }

    @Override
    public void visit(MethodCall exp) {
        exp.method = remap(exp.method);

        add(exp.method);
        if(exp.method.getName().equals("<init>"))
            add(TypeIndex.instance().resolveType(exp.method.ownerName()));

        super.visit(exp);
    }

    public MethodSymbol remap(MethodSymbol method) {
        for(MethodRemapper r : remappers)
            method = r.map(method);

        return method;
    }

    @Override
    public void visit(NewArray exp) {
        add(exp.componentType());

        super.visit(exp);
    }
}
