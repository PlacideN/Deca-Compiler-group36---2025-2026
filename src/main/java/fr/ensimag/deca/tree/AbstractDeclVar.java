package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;

/**
 * Variable declaration
 *
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractDeclVar extends Tree {
    
    /**
     * Implements non-terminal "decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv its "parentEnvironment" corresponds to the "env_exp_sup" attribute and its "current" dictionary corresponds to the "env_exp"
     * @param currentClass 
     * @param index LB offset index of the variable (used for code generation)
     * @param reg Register where the variable is stored (LG or GB)
     */    
    protected abstract void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, int index, Register reg)
            throws ContextualError;
    
    /**
     * Generate code to declare the variable.
     * 
     * @param compiler
     */    
    public abstract void codeGenDeclVar(DecacCompiler compiler);
}
