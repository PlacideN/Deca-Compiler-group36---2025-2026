package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.INT;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;

public class Cast extends AbstractExpr {
    private final AbstractIdentifier typeName;
    private final AbstractExpr expr;

    public Cast(AbstractIdentifier typeName, AbstractExpr expr) {
        Validate.notNull(typeName);
        Validate.notNull(expr);
        this.typeName = typeName;
        this.expr = expr;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        typeName.decompile(s);
        s.print(") ");
        expr.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        typeName.iter(f);
        expr.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        typeName.prettyPrint(s, prefix, false);
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type targetType = typeName.verifyType(compiler);
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        if (!compiler.environmentType.castCompatible(exprType, targetType)) {
            throw new ContextualError("Cannot cast from " + exprType + " to " + targetType, getLocation());
        }
        this.setType(targetType);
        return targetType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        expr.codeGenExpr(compiler, target);
        Type sourceType = expr.getType();
        Type targetType = this.getType();
        if (sourceType.isFloat() && targetType.isInt()) {
            compiler.addInstruction(new INT(target, target));
        } else if (!sourceType.isFloat() || !targetType.isInt()) {
            throw new UnsupportedOperationException("Unsupported cast from " + sourceType + " to " + targetType);
        }
    }
}
