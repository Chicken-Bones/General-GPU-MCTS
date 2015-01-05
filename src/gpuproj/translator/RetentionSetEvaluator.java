package gpuproj.translator;

import gpuproj.srctree.*;

import java.util.*;

/**
 * Recursively calcualtes the retained symbol set as follows
 * 1. All methods and accessors referenced by all statements
 * 2. Classes (structs) referenced by new
 * 3. Fields of retained classes and their types
 * 4. Superclasses of retained classes
 */
public class RetentionSetEvaluator extends StatementVisitor
{
    public Set<Symbol> retained = new HashSet<>();
    public Set<Symbol> search = new HashSet<>();
    public Set<Symbol> handled = new HashSet<>();

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
        return sym.getAnnotation(OCLGlobal.class) != null;
    }

    public static  boolean isOCLStatic(MethodSymbol sym) {
        return sym.getAnnotation(OCLStatic.class) != null;
    }

    public boolean handle(Symbol sym) {
        if(sym instanceof MethodSymbol)
            return handle((MethodSymbol) sym);
        if(sym instanceof FieldSymbol)
            return handle((FieldSymbol) sym);
        if(sym instanceof TypeSymbol)
            return handle((TypeSymbol) sym);

        return false;
    }

    public boolean handle(TypeSymbol sym) {
        return sym instanceof PrimitiveSymbol || sym instanceof ArraySymbol || isOCLGlobal((ClassSymbol)sym);
    }

    public boolean handle(MethodSymbol sym) {
        return sym.source == null || isOCLStatic(sym) || isOCLGlobal(sym.owner());
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
        add(exp.method);
        if(exp.method.getName().equals("<init>"))
            add(TypeIndex.resolveType(exp.method.ownerName()));

        super.visit(exp);
    }

    @Override
    public void visit(NewArray exp) {
        add(exp.componentType());

        super.visit(exp);
    }
}
