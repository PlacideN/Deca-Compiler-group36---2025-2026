package fr.ensimag.deca.tree;


import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

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
        s.print(")(");
        expr.decompile(s);
        s.print(")");
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
        } 
        else if (sourceType.isInt() && targetType.isFloat()) {
            compiler.addInstruction(new FLOAT(target, target));
        } 
        else if (targetType.isClass()) {
            LabelManager lm = compiler.getLabelManager();
            int id = lm.getNextCastLabel();
            
            Label startLoop = lm.castLoop(id);
            Label endCast = lm.castEnd(id);

            // a class cast to null is valid and returns null.
            compiler.addInstruction(new CMP(new NullOperand(), target));
            compiler.addInstruction(new BEQ(endCast));

            GPRegister currentVTable = Register.R0; 
            GPRegister targetVTable = Register.R1;

            compiler.addInstruction(new LOAD(new RegisterOffset(0, target), currentVTable));
            
            // get the address (GB offset) of the target class VTable
            int offsetGB = typeName.getClassDefinition().getVTableOffset();
            compiler.addInstruction(new LEA(new RegisterOffset(offsetGB, Register.GB), targetVTable));

            // inheritance tree search loop
            compiler.addLabel(startLoop);
            compiler.addInstruction(new CMP(targetVTable, currentVTable));
            compiler.addInstruction(new BEQ(endCast)); // jump to end if match

            // get the parent class stored in the first slot of the VTable
            compiler.addInstruction(new LOAD(new RegisterOffset(0, currentVTable), currentVTable));
            
            // if the parent class is null, we are at Object and didn't find a valid cast
            compiler.addInstruction(new CMP(new NullOperand(), currentVTable));
            compiler.addInstruction(new BNE(startLoop));

            compiler.addInstruction(new BRA(compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.IMPOSSIBLE_CAST)));

            compiler.addLabel(endCast);
        }
    }
}
