package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.codegen.VTableLayout;

/**
 * Method declaration.
 *
 * @author gl36
 * @date 11/01/2026
 */
public abstract class AbstractDeclMethod extends Tree {
    /**
     * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 2 (Rule 2.7). 
     * Verify method signature and return type.
     * @param compiler contains the "env_types" attribute
     * @param currentClass corresponds to the "class" attribute
     */
    protected abstract void verifyMethodMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /**
     * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 3 (Rule 3.11). 
     * Verify method body.
     * @param compiler contains the "env_types" attribute
     * @param currentClass corresponds to the "class" attribute
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /** 
     * Add method label to VTable layout.
     */
    protected abstract void codeGenVTable(DecacCompiler compiler, VTableLayout layout, AbstractIdentifier className);

    /** 
     * Generate code for method.
     */
    protected abstract void codeGenMethod(DecacCompiler compiler);

}
