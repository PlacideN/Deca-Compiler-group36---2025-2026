package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl36
 * @date 01/01/2026
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclVar declVar : getList()) {
            declVar.decompile(s);
            s.println(";");
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the "env_exp_r" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */    
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        int index;
        Register reg;
        for (AbstractDeclVar declVar : getList()) {
            if (currentClass == null) {
                // in main
                index = compiler.getStackManager().allocate(); // GB
                reg = Register.GB;
            } else {
                // in a method block
                index = compiler.getStackManager().allocateLB();
                reg = Register.LB;
            }
            declVar.verifyDeclVar(compiler, localEnv, currentClass, index, reg);
        }
    }

     /**
     * Generate code to declare all variables in the list.
     * 
     * @param compiler
     */
    public void codeGenListDeclVar(DecacCompiler compiler) {
        for (AbstractDeclVar declVar : getList()) {
            declVar.codeGenDeclVar(compiler);
        }
    }

}
