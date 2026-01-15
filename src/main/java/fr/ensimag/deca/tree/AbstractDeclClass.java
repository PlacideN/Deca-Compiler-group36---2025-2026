package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

/**
 * Class declaration.
 *
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractDeclClass extends Tree {

   /**
     * Implements non-terminal "decl_class" of [SyntaxeContextuelle] in pass 1.
     * Verify that the class declaration is OK.
     * @param compiler contains the "env_types" attribute
     */
    protected abstract void verifyClass(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Implements non-terminal "decl_class" of [SyntaxeContextuelle] in pass 2.
     * Verify class members (fields and methods).
     * @param compiler contains the "env_types" attribute
     */
    protected abstract void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Implements non-terminal "decl_class" of [SyntaxeContextuelle] in pass 3.
     * Verify method bodies and field initializations.
     * @param compiler contains the "env_types" attribute
     */
    protected abstract void verifyClassBody(DecacCompiler compiler)
            throws ContextualError;
}
