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
    }

    public static class Default extends Statement
    {
        @Override
        public String toString() {
            return "default:";
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
    }

    public Expression key;
    public SwitchBlock body;

    public SwitchStatement(Scope scope, Expression key) {
        this.key = key;
        body = new SwitchBlock(scope);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("switch ").append(key);
        printSub(sb, body);
        return sb.toString();
    }
}
