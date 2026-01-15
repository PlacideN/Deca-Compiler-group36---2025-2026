package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * @author gl36
 * @date 12/01/2026
 */
public class InstanceOf extends AbstractExpr {
    private final AbstractExpr expr;
    private final AbstractIdentifier typeName;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier typeName) {
        Validate.notNull(expr);
        Validate.notNull(typeName);
        this.expr = expr;
        this.typeName = typeName;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expr.decompile(s);
        s.print(" instanceof ");
        typeName.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        typeName.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        typeName.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        Type targetType = typeName.verifyType(compiler);
        if (!(exprType.isClass() || exprType.isNull())) {
            throw new ContextualError("Left side of instanceof must be a class instance or null", 
                getLocation());
        }
        
        if (!targetType.isClass()) {
            throw new ContextualError("Right side of instanceof must be a class name", getLocation());
        }
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


}