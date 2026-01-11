package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Field declaration.
 *
 * @author gl36
 * @date 11/01/2026
 */
public abstract class AbstractDeclField extends Tree {

    /**
     * Pass 2 of [Syntaxe Contextuelle]. Verify field signature (Rule 2.5).
     */
    protected abstract void verifyFieldMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /**
     * Pass 3 of [Syntaxe Contextuelle]. Verify field initialization.
     */
    protected abstract void verifyFieldRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError;
}
