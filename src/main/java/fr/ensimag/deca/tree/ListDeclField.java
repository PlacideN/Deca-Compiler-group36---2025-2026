package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public class ListDeclField extends TreeList<AbstractDeclField> {

    @Override
    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        for (AbstractDeclField f : getList()) {
            f.decompile(s);
        }
    }

    /**
     * Implements non-terminal "list_decl_field" of [SyntaxeContextuelle] in pass 2 (Rule 2.4).
     * Delegate verification to each field.
     * @param compiler contains the "env_types" attribute
     * @param currentClass corresponds to the "class" attribute and contains "super" attribute
     */
    void verifyListFieldMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclField f : getList()) {
            f.verifyFieldMembers(compiler, currentClass);
        }
    }

    /**
     * Implements non-terminal "list_decl_field" of [SyntaxeContextuelle] in pass 3 (Rule 3.6).
     * Delegate verification to each field initialization.
     * @param compiler contains the "env_types" attribute
     * @param currentClass corresponds to the "class" attribute and contains "super" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     */
    void verifyListFieldRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclField f : getList()) {
            f.verifyFieldRValue(compiler, currentClass);
        }
    }

    /**
     * Pass 2 of Gencode. Delegate to each field the generation of its default initialization.
     */
    public void codeGenListDefaultInits(DecacCompiler compiler) {
        for (AbstractDeclField f : getList()) {
            f.codeGenDefaultInit(compiler);
        }
    }

    /**
     * Pass 2 of Gencode. Delegate to each field the generation of its explicit initialization.
     */
    public void codeGenListExplicitInits(DecacCompiler compiler) {
        for (AbstractDeclField f : getList()) {
            f.codeGenExplicitInit(compiler);
        }
    }
    
}
