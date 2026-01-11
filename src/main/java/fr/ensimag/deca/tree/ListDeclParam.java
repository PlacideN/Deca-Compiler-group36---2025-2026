package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

public class ListDeclParam extends TreeList<AbstractDeclParam> {

    @Override
    public void decompile(IndentPrintStream s) {
        boolean isParamFirst = true;
        for (AbstractDeclParam p : getList()) {
            p.decompile(s);
            if (!isParamFirst) {
                s.print(", ");
            } else {
                isParamFirst = false;
            }
        }
    }

    /**
     * Pass 2 of [Syntaxe Contextuelle]. Delegate verification to each parameter (Rule 2.8)
     */
    void verifyListParamType(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclParam p : getList()) {
            p.verifyParamType(compiler);
        }
    }

    /**
     * Pass 3 of [Syntaxe Contextuelle]. Delegate verification to each parameter (Rule 3.12)
     */
    void verifyListParam(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclParam p : getList()) {
            p.verifyParamName(compiler, null);
        }
    }

}
