package fr.ensimag.deca.tree;


/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Greater extends AbstractOpIneq {

    public Greater(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return ">";
    }

}
