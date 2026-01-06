package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;


/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected Type verifyExtraTypes(fr.ensimag.deca.DecacCompiler compiler, Type t1, Type t2) 
            throws ContextualError {
        if (t1.isBoolean() && t2.isBoolean()) {
            return compiler.environmentType.BOOLEAN;
        }

        // TODO: Check for class types compatibility

        return null;
    }

    @Override
    public Priority getPriority() {
        return Priority.EQ;
    }
}
