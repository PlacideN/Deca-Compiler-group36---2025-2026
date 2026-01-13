package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

/**
 * Method declaration.
 *
 * @author gl36
 * @date 11/01/2026
 */
public abstract class AbstractDeclMethod extends Tree {
    /**
     * Pass 2 of [Syntaxe Contextuelle]. Verify method signature and return type (Rule 2.7).
     */
    protected abstract void verifyMethodMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /**
     * Pass 3 of [Syntaxe Contextuelle]. Verify method body (3.11).
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    protected abstract void codeGenMethod(DecacCompiler compiler) throws ContextualError;

}
