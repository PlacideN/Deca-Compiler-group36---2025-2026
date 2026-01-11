package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 * @author gl36
 * @date 11/01/2026
 */
public class DeclParam extends AbstractDeclParam {
    private final AbstractIdentifier type;
    private final AbstractIdentifier paramName;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        Validate.notNull(type);
        Validate.notNull(name);

        this.type = type;
        this.paramName = name;
    }

    @Override
    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        paramName.decompile(s);
    }
    
    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        paramName.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        paramName.iter(f);
    }

    @Override
    protected Type verifyParamType(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    protected void verifyParamName(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
