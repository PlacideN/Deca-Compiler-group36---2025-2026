package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
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
}
