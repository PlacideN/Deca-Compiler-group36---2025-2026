package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

public class Return extends AbstractInst {
    private AbstractExpr expr;

    public Return(AbstractExpr expr) {
        Validate.notNull(expr);
        this.expr = expr;
    }

    @Override
    public void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType) throws ContextualError {

            if (returnType.isVoid()) {
                throw new ContextualError("Return expression cannot be of void type", getLocation());
            }

            // The expression must match the return type (with float to int promotion).
            // Not relevant for the "no-object" sublanguage.
            this.expr = this.expr.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expr.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }  
    
}
