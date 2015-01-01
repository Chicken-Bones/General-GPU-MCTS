package gpuproj.srctree;

import gpuproj.srctree.SwitchStatement.Case;
import gpuproj.srctree.SwitchStatement.Default;

public abstract class StatementVisitor
{
    public void visit(Statement stmt) {
        if(stmt instanceof EmptyStatement);
        else if(stmt instanceof Expression)
            visit((Expression)stmt);
        else if(stmt instanceof ReturnStatement)
            visit((ReturnStatement)stmt);
        else if(stmt instanceof ThrowStatement)
            visit((ThrowStatement)stmt);
        else if(stmt instanceof LocalSymbol)
            visit((LocalSymbol)stmt);
        else if(stmt instanceof CompactLocalDeclaration)
            visit((CompactLocalDeclaration)stmt);
        else if(stmt instanceof Block)
            visit((Block)stmt);
        else if(stmt instanceof LabelledStatement)
            visit((LabelledStatement)stmt);
        else if(stmt instanceof BreakStatement)
            visit((BreakStatement)stmt);
        else if(stmt instanceof ContinueStatement)
            visit((ContinueStatement)stmt);
        else if(stmt instanceof IfStatement)
            visit((IfStatement)stmt);
        else if(stmt instanceof SwitchStatement)
            visit((SwitchStatement)stmt);
        else if(stmt instanceof Case)
            visit((Case)stmt);
        else if(stmt instanceof Default)
            visit((Default)stmt);
        else
            throw new IllegalArgumentException("Unknown statement "+stmt.getClass()+" "+stmt);
    }

    public void visit(ReturnStatement stmt) {
        visit(stmt.exp);
    }

    public void visit(ThrowStatement stmt) {
        visit(stmt.exp);
    }

    public void visit(LocalSymbol local) {
        if(local.init != null)
            visit(local.init);
    }

    public void visit(CompactLocalDeclaration decl) {
        for(LocalSymbol local : decl.locals)
            visit(local);
    }

    public void visit(Block block) {
        for(Statement stmt : block.statements)
            visit(stmt);
    }

    public void visit(LabelledStatement stmt) {
        if(stmt instanceof WhileStatement)
            visit((WhileStatement)stmt);
        else if(stmt instanceof DoStatement)
            visit((DoStatement)stmt);
        else if(stmt instanceof ForStatement)
            visit((ForStatement)stmt);
        else
            throw new IllegalArgumentException("Unknown statement "+stmt.getClass()+" "+stmt);
    }

    public void visit(WhileStatement stmt) {
        visit(stmt.cond);
        visit(stmt.body);
    }

    public void visit(DoStatement stmt) {
        visit(stmt.cond);
        visit(stmt.body);
    }

    public void visit(ForStatement stmt) {
        for(Statement init : stmt.init)
            visit(init);
        visit(stmt.cond);
        for(Statement update : stmt.update)
            visit(update);
        visit(stmt.body);
    }

    public void visit(BreakStatement stmt) {}
    public void visit(ContinueStatement stmt) {}

    public void visit(IfStatement stmt) {
        visit(stmt.cond);
        visit(stmt.then);
        if(stmt.otherwise != null)
            visit(stmt.otherwise);
    }

    public void visit(SwitchStatement stmt) {
        visit(stmt.key);
        visit(stmt.body);
    }

    public void visit(Case stmt) {}
    public void visit(Default stmt) {}

    public void visit(Expression exp) {
        if(exp instanceof Literal)
            visit((Literal)exp);
        else if(exp instanceof Cast)
            visit((Cast)exp);
        else if(exp instanceof Parentheses)
            visit((Parentheses)exp);
        else if(exp instanceof This)
            visit((This)exp);
        else if(exp instanceof NewArray)
            visit((NewArray)exp);
        else if(exp instanceof ArrayAccess)
            visit((ArrayAccess)exp);
        else if(exp instanceof MethodCall)
            visit((MethodCall)exp);
        else if(exp instanceof VariableAccess)
            visit((VariableAccess)exp);
        else if(exp instanceof UnaryOp)
            visit((UnaryOp)exp);
        else if(exp instanceof BinaryOp)
            visit((BinaryOp)exp);
        else if(exp instanceof TernaryOp)
            visit((TernaryOp)exp);
        else
            throw new IllegalArgumentException("Unknown expression "+exp.getClass()+" "+exp);
    }

    public void visit(This exp) {}
    public void visit(Literal exp) {}

    public void visit(Parentheses exp) {
        visit(exp.exp);
    }

    public void visit(Cast exp) {
        visit(exp.exp);
    }

    public void visit(NewArray exp) {
        for(Expression dim : exp.dimensions)
            visit(dim);

        if(exp.init != null)
            visit(exp.init);
    }

    public void visit(ArrayAccess exp) {
        visit(exp.exp);
        visit(exp.index);
    }

    public void visit(MethodCall exp) {
        for(Expression param : exp.params)
            visit(param);
    }

    public void visit(VariableAccess exp) {
        if(exp.exp != null)
            visit(exp.exp);
    }

    public void visit(UnaryOp exp) {
        visit(exp.operand);
    }

    public void visit(BinaryOp exp) {
        visit(exp.op1);
        visit(exp.op2);
    }

    public void visit(TernaryOp exp) {
        visit(exp.cond);
        visit(exp.then);
        visit(exp.otherwise);
    }
}
