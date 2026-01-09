package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                Type type1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
                Type type2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

                if (type1.isInt() && type2.isInt()) {
                    this.setType(compiler.environmentType.INT);
                    return compiler.environmentType.INT;
                } else {
                    throw new ContextualError("Incompatible types for modulo operation: " + type1 + " and " + type2, getLocation());
                }
    }


    @Override
    protected String getOperatorName() {
        return "%";
    }

    @Override
    protected void codeGenOpArith(DecacCompiler compiler, GPRegister dest, GPRegister src) {
        compiler.addInstruction(new REM(src, dest));
    }

}
