package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 * Parameter declaration.
 *
 * @author gl36
 * @date 11/01/2026
 */
public abstract class AbstractDeclParam extends Tree {
    /**
     * Pass 2 of [Syntaxe Contextuelle]. Verify the parameter type and 
     * return it to build the method signature (Rule 2.9).
     */
    protected abstract Type verifyParamType(DecacCompiler compiler) throws ContextualError;

    /**
     * Pass 3 of [Syntaxe Contextuelle]. Verify the parameter name and 
     * add it to the method's local environment (Rule 3.13).
     */
    protected abstract void verifyParamName(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError;
}