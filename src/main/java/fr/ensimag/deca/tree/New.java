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
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;

public class New extends AbstractExpr {
    private final AbstractIdentifier className;

    public New(AbstractIdentifier className) {
        Validate.notNull(className);
        this.className = className;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        className.decompile(s);
        s.print("()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type type = className.verifyType(compiler);
        if (!type.isClass()) {
            throw new ContextualError("New can only be used with class types", getLocation());
        }
        this.setType(type);
        return type;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        int nbFields = className.getClassDefinition().getNumberOfFields();
        int size = 1 + nbFields;

        //on doit tout d'abord allouer l'espace mémoire nécessaire:
        // 1 (la vtable qui stocke les infos de la classe) + le nombre de champs de la classe
        compiler.addInstruction(new NEW(size, target));
        if (!compiler.getCompilerOptions().getNoCheck()) {
            Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.HEAP_OVERFLOW);
            compiler.addInstruction(new BOV(errorLabel));
        }

        // on récupere l'adresse de la vtable, elle est en RegisterOffset(vtableoffset,target)
        int vtableoffset = className.getClassDefinition().getVTableOffset();

        // on la charge dans R0 temporairement pour pouvoir ensuite la mettre dans target 0(GB)
        compiler.addInstruction(new LEA(new RegisterOffset(vtableoffset, Register.GB), Register.R0));

        // on store grâce à l'adresse obtenu dans R0 la vtable en 0(GB)
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, target)));
 
        //on doit construire avec un appel à la classe constructeur
        // on pousse l'adresse de l'objet (dans target) sur la pile
        compiler.addInstruction(new PUSH(target));
        // pour pouvoir appeler le constructeur avec BSR et que les this. aient la bonne adresse
        compiler.addInstruction(new BSR(compiler.getLabelManager().getInitLabel(className.getName().getName())));
        // on dépile l'adresse de l'objet
        compiler.addInstruction(new POP(target));
    }
    
}
