package gpuproj.srctree;

public class SwitchStatement extends Statement
{
    public static class Case extends Statement
    {
        public String key;
        public Case(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "case "+key+":";
        }

        @Override
        public Case copy(Scope scope) {
            return new Case(key);
        }
    }

    public static class Default extends Statement
    {
        @Override
        public String toString() {
            return "default:";
        }

        @Override
        public Statement copy(Scope scope) {
            return new Default();
        }
    }

    public static class SwitchBlock extends Block
    {
        public SwitchBlock(Scope scope) {
            super(scope);
        }

        @Override
        protected void printStatement(StringBuilder sb, Statement stmt) {
            if(stmt instanceof Case || stmt instanceof Default)
                sb.append(indent(stmt.toString()));
            else {
                sb.append(indent(indent(stmt.toString())));
                finishStatement(sb);
            }
        }

        @Override
        public SwitchBlock copy(Scope scope) {
            SwitchBlock copy = new SwitchBlock(scope);
            for(Statement stmt : statements)
                copy.statements.add(stmt.copy(copy.scope));
            copy.index();
            return copy;
        }
    }

    public Expression key;
    public SwitchBlock body;

    public SwitchStatement(Expression key, SwitchBlock body) {
        this.key = key;
        this.body = body;
    }

    public SwitchStatement(Scope scope, Expression key) {
        this(key, new SwitchBlock(scope));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("switch ").append(key);
        printSub(sb, body);
        return sb.toString();
    }

    @Override
    public SwitchStatement copy(Scope scope) {
        return new SwitchStatement(key.copy(scope), body.copy(scope));
    }
}
