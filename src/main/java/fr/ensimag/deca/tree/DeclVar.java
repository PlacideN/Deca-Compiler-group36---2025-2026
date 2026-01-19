package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * @author gl36
 * @date 01/01/2026
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, int index, Register reg)
            throws ContextualError {
        Type varType = this.type.verifyType(compiler);

        if (varType.isVoid()) {
            throw new ContextualError("Variable type cannot be void", this.getLocation());
        }

        this.initialization.verifyInitialization(compiler, varType, localEnv, currentClass);

        VariableDefinition varDef = new VariableDefinition(varType, this.getLocation());
        varDef.setOperand(new RegisterOffset(index, reg));

        try {
            localEnv.declare(this.varName.getName(), varDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("Variable " + this.varName.getName() + " is already defined", this.getLocation());
        }

        this.varName.setDefinition(varDef);
    }

    
    @Override
    public void decompile(IndentPrintStream s) {
        s.print(type.getName().getName() + " " + varName.getName().getName());
        initialization.decompile(s);
    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler) {
        compiler.addComment("Declaration of variable " + varName.getName().getName());
        compiler.getStackManager().allocate();
        DAddr address = this.varName.getVariableDefinition().getOperand();
        this.initialization.codeGenInit(compiler, address);
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
