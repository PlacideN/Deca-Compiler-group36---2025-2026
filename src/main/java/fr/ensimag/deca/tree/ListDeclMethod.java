package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.VTableLayout;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod m : getList()) {
            m.decompile(s);
        }
    }

    /** 
     * Pass 2 of [Syntaxe Contextuelle]. Delegate verification to each method (Rule 2.6)
     */
    void verifyListMethodMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclMethod m : getList()) {
            m.verifyMethodMembers(compiler, currentClass);
        }
    }

    /**
     * Pass 3 of [Syntaxe Contextuelle]. Delegate verification to each method (Rule 3.10)
     */
    void verifyListMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclMethod m : getList()) {
            m.verifyMethodBody(compiler, currentClass);
        }
    }

    /**
     * Pass 1 of Gencode. Add each method to the VTable layout.
     */
    public void codeGenListVTable(DecacCompiler compiler, VTableLayout layout, AbstractIdentifier className) {
        for (AbstractDeclMethod m : getList()) {
            m.codeGenVTable(compiler, layout, className);
        }
    }

    /**
     * Pass 3 of Gencode. Generate assembly code for each method.
     */
    public void codeGenListMethods(DecacCompiler compiler) {
        for (AbstractDeclMethod m : getList()) {
            m.codeGenMethod(compiler);
        }
    }

}