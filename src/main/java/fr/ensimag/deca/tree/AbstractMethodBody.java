package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;

/**
 * Body of a method.
 *
 * @author gl36
 * @date 11/01/2026
 */
public abstract class AbstractMethodBody extends Tree {
    /**
     * Pass 3 of [Syntaxe Contextuelle]. Verify method body (3.11).
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) throws ContextualError;
}
