package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;

/**
 * @author gl36
 * @date 01/01/2026
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    @Override
    public Priority getPriority() {
        return Priority.PRODUCT;
    }

    @Override
    protected void codeGenOpArith(DecacCompiler compiler, GPRegister dest, GPRegister src){
        compiler.addInstruction(new MUL(src, dest));
        Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.OVERFLOW);
        compiler.addInstruction(new BOV(errorLabel));
    }

}
