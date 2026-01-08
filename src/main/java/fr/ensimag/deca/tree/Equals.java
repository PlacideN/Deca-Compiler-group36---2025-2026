package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Equals extends AbstractOpExactCmp {

    public Equals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "==";
    }    

    @Override
    protected void codeGenOpCmp(DecacCompiler compiler, GPRegister target) {
        compiler.addInstruction(new SEQ(target)); // set if equal
    }

    
}
