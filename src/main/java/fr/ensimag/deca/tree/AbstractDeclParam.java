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
     * Implements non-terminal "decl_param" of [SyntaxeContextuelle] in pass 2 (Rule 2.9).
     * @param compiler contains the "env_types" attribute
     * @return Type corresponds to the "type" attribute
     */
    protected abstract Type verifyParamType(DecacCompiler compiler) throws ContextualError;

    /**
     * Implements non-terminal "decl_param" of [SyntaxeContextuelle] in pass 3 (Rule 3.13).
     * @param compiler contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute (params)
     * @param index index of the parameter on the stack (used for code generation)
     */
    protected abstract void verifyParamName(DecacCompiler compiler, EnvironmentExp localEnv, int index) throws ContextualError;
}