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
     * Pass 2 of [Syntaxe Contextuelle]. Delegate verification to each field (Rule 2.4)
     */
    void verifyListFieldMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclField f : getList()) {
            f.verifyFieldMembers(compiler, currentClass);
        }
    }

    /**
     * Pass 3 of [Syntaxe Contextuelle]. Delegate verification to each field (Rule 3.6)
     */
    void verifyListFieldRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclField f : getList()) {
            f.verifyFieldRValue(compiler, localEnv, currentClass);
        }
    }

    
}
