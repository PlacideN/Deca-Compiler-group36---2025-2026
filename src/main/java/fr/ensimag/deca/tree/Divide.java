package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;

/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    @Override
    public Priority getPriority() {
        return Priority.PRODUCT;
    }

    @Override
    protected void codeGenOpArith(DecacCompiler compiler, GPRegister dest, GPRegister src) {
        if (getLeftOperand().getType().isInt() && getRightOperand().getType().isInt()) {
            compiler.addInstruction(new QUO(src, dest));
            if (!compiler.getCompilerOptions().getNoCheck()) {
                Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.DIV_BY_ZERO);
                compiler.addInstruction(new BOV(errorLabel));
            }
        } else {
            compiler.addInstruction(new DIV(src, dest)); // only works for floats
            if (!compiler.getCompilerOptions().getNoCheck()) {
                Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.OVERFLOW);
                compiler.addInstruction(new BOV(errorLabel));
            }
        }
    }

}
