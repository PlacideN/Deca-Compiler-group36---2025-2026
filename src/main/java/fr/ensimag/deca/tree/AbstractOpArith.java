package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type type1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Type arithType = compiler.environmentType.typeArithOp(type1, type2);
        if (arithType == null) {
            throw new ContextualError("Incompatible types for arithmetic operation: " 
                    + type1 + " and " + type2, this.getLocation());
        }
        if(arithType.isFloat()) {
            wrapWithConvFloat(compiler, localEnv, currentClass, type1, type2);
        }
        this.setType(arithType);
        return this.getType();              
    }

    /**
     * Generate code for the arithmetic operation.
     * @param compiler
     * @param dest destination register. Contains left operand at entry, result after operation.
     * @param src source register. Contains right operand.
     */
    protected abstract void codeGenOpArith(DecacCompiler compiler, GPRegister dest, GPRegister src);

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target){
        this.getLeftOperand().codeGenExpr(compiler, target);
        GPRegister temp = compiler.getRegisterManager().allocateRegister();
        this.getRightOperand().codeGenExpr(compiler, temp);

        // if both operands are in the same register (ie. spilling on stack)
        // we need to move one of the values
        if (target.getNumber() == temp.getNumber()) {
            // temp = last register holds the right op
            // target, allocated before temp was pushed on the stack during right op eval
            
            compiler.addInstruction(new LOAD(temp, GPRegister.R0));
            compiler.getRegisterManager().freeRegister(temp);

            this.codeGenOpArith(compiler, target, GPRegister.R0);
        } else {
            // standard case: enough registers available
            this.codeGenOpArith(compiler, target, temp);
            compiler.getRegisterManager().freeRegister(temp);
        }
    }
}
