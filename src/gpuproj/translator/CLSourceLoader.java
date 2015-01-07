package gpuproj.translator;

import gpuproj.srctree.SourceReader;
import gpuproj.srctree.SourceUtil;
import gpuproj.srctree.Symbol;
import gpuproj.srctree.TypeIndex;
import gpuproj.translator.CLProgramBuilder.Declaration;
import gpuproj.translator.CLProgramBuilder.Implementation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CLSourceLoader
{
    public static class CLDecl implements Declaration
    {
        public List<String> identifiers;
        public String code;

        public CLDecl(String code, List<String> identifiers) {
            this.code = code;
            this.identifiers = identifiers;
        }

        public CLDecl(String code, String identifier) {
            this(code, Arrays.asList(identifier));
        }

        @Override
        public List<String> identifiers() {
            return identifiers;
        }

        @Override
        public String declare() {
            return code;
        }
    }

    public static class CLImpl implements Implementation
    {
        public String code;

        public CLImpl(String code) {
            this.code = code;
        }

        @Override
        public String implement() {
            return code;
        }
    }

    public CLProgramBuilder program;

    public CLSourceLoader(CLProgramBuilder program) {
        this.program = program;
    }

    public void load(String path) {
        SourceReader r = new SourceReader(TypeIndex.provideSource(path));

        while(!r.end()) {
            if(r.charAt(r.pos) == '#') {
                preprocess(r.readLine());
                continue;
            }

            String stmt = r.readStatement();
            if(stmt.isEmpty())
                continue;

            if(stmt.startsWith("enum"))
                loadEnum(stmt);
            else if(stmt.startsWith("typedef")) {
                int end = r.indexOf(';');
                stmt += r.substring(r.pos, end);
                r.pos = end+1;

                loadTypedef(stmt);
            } else if(SourceUtil.methodOrField(stmt) == Symbol.FIELD_SYM)
                loadVariable(stmt);
            else
                loadFunction(stmt);
        }
    }

    private void loadVariable(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.seekStart("=");
        r.rollbackIdentifier();
        String name = r.readElement();
        program.declare(new CLDecl(stmt+';', name));
    }

    private void loadFunction(final String stmt) {
        if(stmt.endsWith("}")) {
            program.implement(new CLImpl(stmt));
        } else {
            SourceReader r = new SourceReader(stmt);
            r.seekStart("(");
            r.rollbackIdentifier();
            String name = r.readElement();
            program.declare(new CLDecl(stmt+';', name));
        }
    }

    private void loadTypedef(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.pos = r.source.length();
        r.rollbackIdentifier();
        String name = r.readElement();
        program.declare(new CLDecl(stmt+';', name));
        //TODO get identifiers for typedef enum
    }

    private void loadEnum(String stmt) {
        SourceReader r = new SourceReader(stmt);
        r.readElement();//enum
        List<String> identifiers = new LinkedList<String>();
        for(String s : new SourceReader(SourceReader.expand(r.readElement())).readList())
            identifiers.add(new SourceReader(s).readElement());

        program.declare(new CLDecl(stmt+';', identifiers));
    }

    private void preprocess(String line) {
        SourceReader r = new SourceReader(line);
        String cmd = r.readElement();
        if(cmd.equals("#include")) {
            String path = SourceReader.expand(r.readElement());
            load(path);
        }
        //TODO support preprocessor conditionals and #define
    }
}
