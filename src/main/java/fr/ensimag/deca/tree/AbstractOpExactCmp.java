package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
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

        // comparaison de bool
        if (t1.isBoolean() && t2.isBoolean()) {
            return compiler.environmentType.BOOLEAN;
        }

        // comparaison d'objets et de null
        // règle : On peut comparer (Classe ou Null) avec (Classe ou Null)
        boolean t1IsObjOrNull = t1.isClass() || t1.isNull();
        boolean t2IsObjOrNull = t2.isClass() || t2.isNull();

        if (t1IsObjOrNull && t2IsObjOrNull) {
            // on vérifie la compatibilité 
            if (compiler.environmentType.assignCompatible(t1, t2) || 
                compiler.environmentType.assignCompatible(t2, t1)) {
                return compiler.environmentType.BOOLEAN;
            }
        }
        // si incompatible
        return null;
    }

    @Override
    public Priority getPriority() {
        return Priority.EQ;
    }
}
