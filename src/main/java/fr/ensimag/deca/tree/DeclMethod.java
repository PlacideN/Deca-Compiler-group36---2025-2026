package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.VTableLayout;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

/**
 * @author gl36
 * @date 11/01/2026
 */
public class DeclMethod extends AbstractDeclMethod {
    private final AbstractIdentifier returnType;
    private final AbstractIdentifier methodName;
    private final ListDeclParam params;
    private final AbstractMethodBody methodBody;

    public DeclMethod(AbstractIdentifier returnType, AbstractIdentifier methodName, ListDeclParam params, AbstractMethodBody methodBody) {
        Validate.notNull(returnType);
        Validate.notNull(methodName);
        Validate.notNull(params);
        Validate.notNull(methodBody);

        this.returnType = returnType;
        this.methodName = methodName;
        this.params = params;
        this.methodBody = methodBody;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        returnType.decompile(s);
        s.print(" ");
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(") ");
        methodBody.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        returnType.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        methodBody.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        returnType.iter(f);
        methodName.iter(f);
        params.iter(f);
        methodBody.iter(f);
    }
    
    @Override
    protected void verifyMethodMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        Type type = returnType.verifyType(compiler);
        Signature sig = params.verifyListParamType(compiler);
        ClassDefinition superClassDef = currentClass.getSuperClass();
        ExpDefinition inherited = superClassDef.getMembers().get(methodName.getName());

        int index;
        if (inherited != null) {
            MethodDefinition inheritedMethodDef = inherited.asMethodDefinition("The name " + methodName.getName().getName() + " is already used by a field in superclass", getLocation());
            
            if (!sig.equals(inheritedMethodDef.getSignature())) {
                throw new ContextualError("Method '" + methodName.getName() 
                    + "' signature does not match inherited method.", getLocation());
            }

            if (!compiler.environmentType.subtype(type, inheritedMethodDef.getType())) {
                throw new ContextualError("Method '" + methodName.getName() 
                    + "' return type is not a valid subtype of inherited method.", getLocation());
            }

            index = inheritedMethodDef.getIndex();
        } else {
            index = currentClass.incNumberOfMethods();
        }

        MethodDefinition methodDef = new MethodDefinition(type, getLocation(), sig, index);
        methodDef.setClassName(currentClass.getType().getName());
        try {
            currentClass.getMembers().declare(methodName.getName(), methodDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("Method '" + methodName.getName() + "' is already defined in class '" + currentClass.getClass().getName() + "'.", getLocation()); 
        }

        methodName.setDefinition(methodDef);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass) 
            throws ContextualError {
        compiler.getStackManager().resetLB();
        EnvironmentExp envExpSup = currentClass.getMembers();
        EnvironmentExp localEnv = params.verifyListParam(compiler, envExpSup);

        Type retType = methodName.getMethodDefinition().getType();

        methodBody.verifyMethodBody(compiler, localEnv, currentClass, retType);
    }

    @Override
    protected void codeGenVTable(DecacCompiler compiler, VTableLayout layout, AbstractIdentifier className) {
        String methodName = this.methodName.getName().getName();
        String currentClass = className.getName().getName();
    
        Label label = compiler.getLabelManager().getMethodLabel(currentClass, methodName);
        layout.addMethod(methodName, label.toString());
    }


    @Override
    protected void codeGenMethod(DecacCompiler compiler) {
        String name = methodName.getName().getName();
        String className = methodName.getMethodDefinition().getClassName().getName();
        Label methodLabel = compiler.getLabelManager().getMethodLabel(className, name);
        Label endLabel = compiler.getLabelManager().getMethodEndLabel(className, name);
        compiler.getLabelManager().setCurrentMethodEndLabel(endLabel);
        compiler.addLabel(methodLabel);

        compiler.getRegisterManager().reset();
        compiler.getStackManager().resetLB();

        IMAProgram body = new IMAProgram();
        IMAProgram init = compiler.getProgram();
        IMAProgram registerSave = new IMAProgram();
        compiler.setProgram(body);

        methodBody.codeGenMethodBody(compiler);

        // case where no return is found in the method body
        Type returnType = methodName.getMethodDefinition().getType();
        if (!returnType.isVoid() && !compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new WSTR("Error: Exit method " + name + " without return"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        compiler.addLabel(endLabel);
        compiler.setProgram(registerSave);
        compiler.addComment("Sauvegarde des registres");
        compiler.getRegisterManager().saveRegisters(compiler);
        compiler.setProgram(init);

        int localVariablesSize = compiler.getStackManager().getCurrentLBOffset();
        int nbSavedRegs = compiler.getRegisterManager().getNbSavedRegisters();
        int maxTempSpace = compiler.getRegisterManager().getMaxTempStackUsage();
        int totalStackDepth = nbSavedRegs + maxTempSpace + localVariablesSize;

        // tsto
        if (!compiler.getCompilerOptions().getNoCheck() && totalStackDepth > 0) {
            compiler.addInstruction(new TSTO(totalStackDepth));
            compiler.addInstruction(new BOV(compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.STACK_OVERFLOW)));
        }
        
        // addsp
        if (localVariablesSize > 0) {
            compiler.addInstruction(new ADDSP(localVariablesSize));
        }

        init.append(registerSave);    
        init.append(body);  
        
        compiler.addComment("Restauration des registres");
        compiler.getRegisterManager().restoreRegisters(compiler); 
        compiler.addInstruction(new RTS());
    }
}