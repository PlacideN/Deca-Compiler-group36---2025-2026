package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BOV;

/**
 * @author gl36
 * @date 01/01/2026
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    public Priority getPriority() {
        return Priority.SUM;
    }


    @Override
    protected void codeGenOpArith(DecacCompiler compiler, GPRegister dest, GPRegister src) {
        compiler.addInstruction(new ADD(src, dest));
        Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.OVERFLOW);
        compiler.addInstruction(new BOV(errorLabel));
    }
}
