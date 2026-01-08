package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

/**
 * Full if/else if/else statement.
 *
 * @author gl36
 * @date 01/01/2026
 */
public class IfThenElse extends AbstractInst {
    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type condType = condition.verifyExpr(compiler, localEnv, currentClass);

        if (!condType.isBoolean()) {
            throw new ContextualError("If statement condition must be boolean", getLocation());
        }

        thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
        elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        LabelManager lm = compiler.getLabelManager();
        int num = lm.getNextIfLabel();

        // create labels 
        Label elseLabel = lm.ifElseLabel(num);
        Label endLabel = lm.ifEndLabel(num);

        // condition management
        GPRegister cond = compiler.getRegisterManager().allocateRegister();
        condition.codeGenExpr(compiler, cond);
        compiler.addInstruction(new CMP(0, cond));
        compiler.getRegisterManager().freeRegister();
        compiler.addInstruction(new BEQ(elseLabel)); // move to else if condition is false

        // then
        thenBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(endLabel)); // jump to end (skip else)

        // else
        compiler.addLabel(elseLabel);
        elseBranch.codeGenListInst(compiler);

        // end
        compiler.addLabel(endLabel);

    }

    @Override
    public void decompile(IndentPrintStream s) {
        //permet de décompiler les imbrications de if: if ... else if ... else
        s.print("if (");
        condition.decompile(s);
        s.println(") {");
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.print("}");
        
        if (!elseBranch.isEmpty()) {
            s.println(" else {");
            s.indent();
            elseBranch.decompile(s);
            s.unindent();
            s.print("}");
        }
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
