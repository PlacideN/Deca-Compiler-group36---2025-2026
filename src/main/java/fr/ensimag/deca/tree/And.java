package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;

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
        LabelManager lm = compiler.getLabelManager();
        Label endLabel = lm.boolOpEndLabel(lm.getNextBoolOpLabel());
    
        // lazy evaluation => evaluate left operand first
        this.getLeftOperand().codeGenExpr(compiler, target);

        // skip if left operand is false
        compiler.addInstruction(new CMP(0, target));
        compiler.addInstruction(new BEQ(endLabel)); // jump if equal (to 0)
        
        // otherwise evaluate right operand
        this.getRightOperand().codeGenExpr(compiler, target);

        compiler.addLabel(endLabel);
    }

}
