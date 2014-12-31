package gpuproj.srctree;

import java.util.List;

public class CompactLocalDeclaration extends Statement
{
    public List<LocalSymbol> locals;

    public CompactLocalDeclaration(List<LocalSymbol> locals) {
        this.locals = locals;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(locals.get(0).type).append(' ');
        for(int i = 0; i < locals.size(); i++) {
            if(i > 0) sb.append(", ");
            LocalSymbol local = locals.get(i);
            sb.append(local.name);
            if(local.init != null)
                sb.append(" = ").append(local.init);
        }

        return sb.toString();
    }
}
