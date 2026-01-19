package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;

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
        //affiche en ass ima :  A instance of B avec A une expr
        LabelManager lm = compiler.getLabelManager();
        int id = lm.getNextInstanceOfLabel();
        
        Label startLoop = lm.instanceOfLoop(id);
        Label isInstance = lm.instanceOfTrue(id);
        Label endLabel = lm.instanceOfEnd(id);

        // mets dans R2 l'objet à gauche du instanceof (new A() instanceof B ou juste A instance of B ...)
        expr.codeGenExpr(compiler, target); 
        // on verifie que l'adresse n'est pas null
        compiler.addInstruction(new CMP(new NullOperand(), target));
        compiler.addInstruction(new BEQ(endLabel));

        // on met vtable dans target(avant target contenait l'adresse de l'objet)
        compiler.addInstruction(new LOAD(new RegisterOffset(0, target), target));

        // get the memory address
        GPRegister temp = Register.R0;
        RegisterOffset offset = new RegisterOffset(typeName.getClassDefinition().getVTableOffset(), Register.GB);
        compiler.addInstruction(new LEA(offset, temp));


        compiler.addLabel(startLoop);

        // on compare l'adresse de la vtable de l'objet avec celle de la classe cible si égale alors c'est une instance
        GPRegister tempReg = compiler.getRegisterManager().allocateRegister();

        compiler.addInstruction(new LEA(new RegisterOffset(right_Offset, Register.GB), tempReg));

        compiler.addInstruction(new CMP(tempReg, target));
        compiler.getRegisterManager().freeRegister(tempReg);


    
        compiler.addInstruction(new BEQ(isInstance));

        // on remonte dans la hiérarchie des classes, la classe mère est à l'offset 0 de la vtable
        compiler.addInstruction(new LOAD(new RegisterOffset(0, target),target));

        // on revient au début de la boucle si l'addresse n'est pas nulle
        compiler.addInstruction(new CMP(new NullOperand(),target));
        compiler.addInstruction(new BNE(startLoop));

        // Si l'adresse est nulle, on met 0 dans target (faux)
        compiler.addInstruction(new LOAD(0,target));
        compiler.addInstruction(new BRA(endLabel));

        // Si c'est une instance, on met 1 dans target (vrai)
        compiler.addLabel(isInstance);
        compiler.addInstruction(new LOAD(1, target));

        compiler.addLabel(endLabel);



    }


}