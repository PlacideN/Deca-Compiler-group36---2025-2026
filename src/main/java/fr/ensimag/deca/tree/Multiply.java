package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.MUL;

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
    }

}
