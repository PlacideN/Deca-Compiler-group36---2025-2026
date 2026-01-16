package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.log4j.Logger;

/**
 *
 * @author gl36
 * @date 01/01/2026
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        for (AbstractDeclClass c : getList()) {
            c.verifyClass(compiler);
        }
        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass c : getList()) {
            c.verifyClassMembers(compiler);
        }
    }
    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass c : getList()) {
            c.verifyClassBody(compiler);
        }
    }

    /**
     * Pass 1 of Gencode. Delegate VTable generation to each class.
     */
    public void codeGenListVTable(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.codeGenVTable(compiler);
        }
    }

    /**
     * Pass 2 of Gencode. Delegate fields initialization to each class.
     */
    public void codeGenListInit(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.codeGenInit(compiler);
        }
    }

    /**
     * Pass 3 of Gencode. Delegate code generation for method bodies to each class.
     */
    public void codeGenListMethods(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.codeGenMethods(compiler);
        }
    }


}
