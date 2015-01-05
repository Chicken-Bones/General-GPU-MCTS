package gpuproj.translator;

import gpuproj.srctree.*;
import gpuproj.translator.OCLProgramBuilder.Declaration;
import gpuproj.translator.OCLProgramBuilder.Implementation;

import java.util.List;

public class JavaTranslator extends StatementVisitor
{
    public OCLProgramBuilder program;

    public class MethodWrapper implements Declaration, Implementation
    {
        public MethodSymbol method;

        public MethodWrapper(MethodSymbol method) {
            this.method = method;
        }

        @Override
        public String getName() {
            return method.getName();
        }

        private void declare(StringBuilder sb) {
            sb.append(method.returnType).append(' ').append(method.fullname);
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

    public JavaTranslator(OCLProgramBuilder program) {
        this.program = program;
    }

    public void addStruct(ClassSymbol sym) {

    }

    /**
     * Designates a type as global to the scope of the program being built.
     * Instance fields and methods will be made global.
     * Any method parameters with this type will be removed
     * This method should be called before any references to this type are made as only new methods will be transformed
     */
    public void addGlobalInstance(ClassSymbol sym) {

    }

    public void addGlobalMethod(MethodSymbol sym, List params) {
        //flatten
        //search
    }

    public MethodSymbol getMethod(String name) {
        return ((MethodWrapper)program.getDeclaration(name)).method;
    }
}
