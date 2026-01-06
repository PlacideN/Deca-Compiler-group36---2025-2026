package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    public Priority getPriority() {
        return Priority.AND;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target){
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
