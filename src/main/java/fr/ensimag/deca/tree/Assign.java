package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftType = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);

        AbstractExpr validRvalue = this.getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftType);

        this.setRightOperand(validRvalue);
        this.setType(leftType);
        return leftType;
    }


    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    public Priority getPriority() {
        return Priority.ASSIGN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target){
        compiler.addComment("Assignement to " + this.getLeftOperand().decompile());

        //load the value of the right operand into target
        this.getRightOperand().codeGenExpr(compiler, target); 
        ExpDefinition def = this.getLeftOperand().getExpDefinition();

        if (def.isField()) {
            FieldDefinition fieldDef = (FieldDefinition) def;
            
            // load 'this' (stored at -2(LB)) into a register
            GPRegister temp = compiler.getRegisterManager().allocateRegister();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), temp));
            
            // check for null dereference
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new CMP(new NullOperand(), temp));
                compiler.addInstruction(new BEQ(compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.NULL_DEREFERENCE)));
            }
            
            // store target value into the field
            compiler.addInstruction(new STORE(target, new RegisterOffset(fieldDef.getIndex(), temp)));
            compiler.getRegisterManager().freeRegister(temp);
            
        } else {
            // store the value in the address
            compiler.addInstruction(new STORE(target, def.getOperand()));
        }
    }

}
