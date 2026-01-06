package fr.ensimag.deca.tree;


/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractOpIneq extends AbstractOpCmp {

    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Priority getPriority() {
        return Priority.CMP;
    }


}
