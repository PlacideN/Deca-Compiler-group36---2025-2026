package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

/**
 * Field declaration.
 *
 * @author gl36
 * @date 11/01/2026
 */
public abstract class AbstractDeclField extends Tree {

    /**
     * Implements non-terminal "decl_field" of [SyntaxeContextuelle] in pass 2 (Rule 2.5)
     * @param compiler contains the "env_types" attribute
     * @param currentClass corresponds to the "class" attribute contains "super" 
     */
    protected abstract void verifyFieldMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    
    /**
     * Implements non-terminal "decl_field" of [SyntaxeContextuelle] in pass 3 (Rule 3.7)
     * @param compiler contains the "env_types" attribute
     * @param currentClass corresponds to the "class" attribute
     */
    protected abstract void verifyFieldRValue(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /**
     * Pass 1 of Gencode. Assign an index to the field definition.
     */
    protected abstract void codeGenFieldOffset(int offset);

    /** 
     * Pass 2 of Gencode. Generate assembly code for the default field initialization.
     */
    protected abstract void codeGenDefaultInit(DecacCompiler compiler);

    /**
     * Pass 2 of Gencode. Generate assembly code for the field initialization with explicit initialization.
     */
    protected abstract void codeGenExplicitInit(DecacCompiler compiler);
}
