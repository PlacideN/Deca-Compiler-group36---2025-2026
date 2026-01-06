package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
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

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target){
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
