package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl36
 * @date 01/01/2026
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) {
                Type type = compiler.environmentType.FLOAT;
                this.setType(type);
                return type;
    }


    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        // 1. On génère le code de l'entier et on met le résultat dans 'target'
        getOperand().codeGenExpr(compiler, target);
        
        // 2. On convertit le contenu de 'target' (int) en float (toujours dans 'target')
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.FLOAT(target, target));
        }

}
