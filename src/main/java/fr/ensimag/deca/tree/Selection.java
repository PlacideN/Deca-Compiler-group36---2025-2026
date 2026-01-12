package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.Type;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * @author gl36
 * @date 12/01/2026
 */
public class Selection extends AbstractLValue {
    private final AbstractExpr object;
    private final AbstractIdentifier fieldName;

    public Selection(AbstractExpr object, AbstractIdentifier fieldName) {
        Validate.notNull(object);
        Validate.notNull(fieldName);
        this.object = object;
        this.fieldName = fieldName;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        s.print(".");
        fieldName.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        fieldName.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, true);
    }

    @Override
    public ExpDefinition getExpDefinition() {
        return fieldName.getExpDefinition();
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
