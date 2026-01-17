package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.deca.context.ParamDefinition;
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
        Type paramType = type.verifyType(compiler);
        if (paramType.isVoid()) {
            throw new ContextualError("Parameter cannot be of type void", getLocation());
        }
        return paramType;
    }


    protected void verifyParamName(DecacCompiler compiler, EnvironmentExp localEnv, int index) throws ContextualError {
        Type paramType = type.verifyType(compiler);
        ParamDefinition paramDef = new ParamDefinition(paramType, getLocation());
        paramDef.setOperand(new RegisterOffset(index, Register.LB));

        try {
            localEnv.declare(paramName.getName(), paramDef);
            paramName.setDefinition(paramDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("Parameter " + paramName.getName().getName() + " is already defined", getLocation());
        }
    }

}
