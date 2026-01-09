package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
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
    protected void codeGenOpArith(DecacCompiler compiler, GPRegister leftReg, GPRegister rightReg) {
        if (getLeftOperand().getType().isInt() && getRightOperand().getType().isInt()) {
            compiler.addInstruction(new QUO(leftReg, rightReg));
        } else {
            compiler.addInstruction(new DIV(leftReg, rightReg)); // only works for floats
        }
    }

}
