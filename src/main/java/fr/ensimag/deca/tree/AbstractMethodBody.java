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
     * Implements non-terminal "method_body" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv its "parentEnvironment" corresponds to "env_exp_sup" attribute (the class) and the "current" corresponds to the "env_exp" attribute (the parameters)
     * @param currentClass 
     * corresponds to "class" attribute
     * @param returnType
     * corresponds to "return" attribute
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) throws ContextualError;

    /**
     * Generate code for the method body.
     * 
     * @param compiler
     */
    protected abstract void codeGenMethodBody(DecacCompiler compiler);
}
