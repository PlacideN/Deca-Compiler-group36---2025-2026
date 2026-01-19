package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.InlinePortion;
/** 
 * @author gl36
 * @date 11/01/2026
 */
public class MethodAsmBody extends AbstractMethodBody {
    private final AbstractStringLiteral asmCode;

    public MethodAsmBody(AbstractStringLiteral asmCode) {
        this.asmCode = asmCode;
    }

    @Override
    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        s.println("{");
        s.indent();
        asmCode.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        asmCode.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        asmCode.iter(f);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) throws ContextualError {
        this.asmCode.verifyExpr(compiler, localEnv, currentClass);
    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        String Ass = asmCode.getValue();
        if (Ass.startsWith("\"") && Ass.endsWith("\"")) {
            Ass = Ass.substring(1, Ass.length() - 1); // remove quotes from string
        }
        compiler.add(new InlinePortion(Ass));
    }

}