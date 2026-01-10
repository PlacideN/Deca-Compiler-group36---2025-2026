package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.CMP;

/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type type1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // Exact comparison (==, !=) operators support boolean and class types
        Type resultType = verifyExtraTypes(compiler, type1, type2);

        if (resultType != null) {
            this.setType(resultType);
            return this.getType();
        }

        // Default behavior for comparison operators (only numeric types)
        Type arithType = compiler.environmentType.typeArithOp(type1, type2);
        if (arithType == null) {
            throw new ContextualError("Incompatible types for comparison: " 
                    + type1 + " and " + type2, this.getLocation());
        }
        if(arithType.isFloat()) {
            wrapWithConvFloat(compiler, localEnv, currentClass, type1, type2);
        }

        this.setType(compiler.environmentType.BOOLEAN);
        return this.getType();
    }

    /** Implements default behavior for comparison operators (<, <=, >, >=). 
     *  @param compiler DecacCompiler instance
     *  @param t1 Type of the left operand
     *  @param t2 Type of the right operand 
     *  @return null because these operators only accept numeric types.
     */
    protected Type verifyExtraTypes(DecacCompiler compiler, Type t1, Type t2) 
            throws ContextualError {
        return null;
    }

    /**
     * Generate the comparison code (Scc instruction) for the comparison operator (<, <=, >, >=).
     * @param compiler
     * @param target register used to store the result
     */
    protected abstract void codeGenOpCmp(DecacCompiler compiler, GPRegister target);

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target){
        compiler.addComment("Binary comparison operation: " + this.getOperatorName());
        
        this.getLeftOperand().codeGenExpr(compiler, target);
        GPRegister temp = compiler.getRegisterManager().allocateRegister();
        this.getRightOperand().codeGenExpr(compiler, temp);

        compiler.addInstruction(new CMP(temp, target));
        compiler.getRegisterManager().freeRegister(temp);

        this.codeGenOpCmp(compiler, target); // adds specific Scc instruction
    }

}
