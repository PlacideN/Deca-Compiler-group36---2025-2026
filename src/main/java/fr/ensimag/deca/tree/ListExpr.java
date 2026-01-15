package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl36
 * @date 01/01/2026
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        boolean isExpFirst = true;
        for (AbstractExpr expr : getList()) {
            expr.decompile(s);
            if (!isExpFirst) {
                s.print(", ");
            } else {
                isExpFirst = false;
            }
        }
    }

    public void verifyListExprSignature(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Signature sig) throws ContextualError {
        
        if (getList().size() != sig.size()) {
            throw new ContextualError("Expected " + sig.size() + " args, got " + getList().size(), getLocation());
        }

        for (int i = 0; i < getList().size(); i++) {
            Type expectedType = sig.paramNumber(i);
            AbstractExpr validParam = getList().get(i).verifyRValue(compiler, localEnv, currentClass, expectedType);
            this.set(i, validParam);
        }
    }
}
