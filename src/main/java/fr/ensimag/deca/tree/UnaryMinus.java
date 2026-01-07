package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * @author gl36
 * @date 01/01/2026
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type operandType = getOperand().verifyExpr(compiler, localEnv, currentClass);
        if (!operandType.isNumeric()) {
            throw new ContextualError("Operand of UnaryMinus must be of numeric type", getLocation());
        }
        this.setType(operandType);
        return this.getType();
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        // 1. On calcule la valeur de l'expression (ex: 1.25) dans target
        getOperand().codeGenExpr(compiler, target);
        
        // 2. On prend l'opposé (target = -target)
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.OPP(target, target));
    }
    

}
