package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.LOAD;


/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue" 
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments 
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute            
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {
        Type actualType = this.verifyExpr(compiler, localEnv, currentClass);

        if (!compiler.environmentType.assignCompatible(expectedType, actualType)) {
            throw new ContextualError("Incompatible types in Rvalue = " + this.decompile() + ": cannot assign " + actualType + " to " + expectedType, this.getLocation());
        }

        // Float promotion if expression is int and expected type is float
        if (expectedType.isFloat() && actualType.isInt()) {
            ConvFloat conv = new ConvFloat(this);
            conv.verifyExpr(compiler, localEnv, currentClass);
            return conv;
        }

        return this; // no conversion needed return original expression
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        this.verifyExpr(compiler, localEnv, currentClass);
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                Type type = this.verifyExpr(compiler, localEnv, currentClass);
                if (!type.isBoolean()) {
                    throw new ContextualError("Condition must be a boolean. Found: " + type, 
                                            this.getLocation());
            }
    }


    /**
     * Generate code to evaluate the expression and store the result.
     * 
     * @param compiler
     * @param target general purpose register where to store the result
     */
    protected abstract void codeGenExpr(DecacCompiler compiler, GPRegister target);

    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        compiler.addComment("Print expression" + (printHex ? " (Hex)" : ""));

        // use a an available register (starting from R2) to store the expression result
        GPRegister res = compiler.getRegisterManager().allocateRegister();
        this.codeGenExpr(compiler, res);

        // WINT, WFLOAT, etc. instructions fetch the value from R1
        compiler.addInstruction(new LOAD(res, GPRegister.R1));

        if (this.getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (this.getType().isFloat()) {
            if (printHex) {
                compiler.addInstruction(new WFLOATX());
            } else {
                compiler.addInstruction(new WFLOAT());
            }
        } else {
            // String type doesn't have an expression to evaluate 
            // It is managed by StringLiteral directly
            // => Should never happen if contextual verification is correct
            throw new DecacInternalError("Type " + this.getType() + " should have been handled by a specific override or caught in contextual verification.");
        }

        compiler.getRegisterManager().freeRegister(res);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // use a temporary register to evaluate the expression
        GPRegister temp = compiler.getRegisterManager().allocateRegister();
        this.codeGenExpr(compiler, temp);
        compiler.getRegisterManager().freeRegister(temp);
    }
    

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    public enum Priority {
        ASSIGN,
        OR,
        AND,
        EQ,
        CMP,
        SUM,
        PRODUCT,
        UNARY,
        ATOMIC // identifiers, literals, etc.
    }

    public Priority getPriority(){
        return Priority.ATOMIC;
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }
}
