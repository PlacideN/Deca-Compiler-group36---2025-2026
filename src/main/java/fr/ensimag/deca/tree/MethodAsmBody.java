package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/** 
 * @author gl36
 * @date 11/01/2026
 */
public class MethodAsmBody extends AbstractMethodBody {
    private final String asmCode;

    public MethodAsmBody(String asmCode) {
        this.asmCode = asmCode;
    }

    @Override
    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        s.println("{");
        s.indent();
        s.println(asmCode);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        // leaf node => nothing to print
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to iterate over
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}