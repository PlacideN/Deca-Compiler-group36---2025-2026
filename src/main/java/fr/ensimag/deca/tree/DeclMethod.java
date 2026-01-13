package fr.ensimag.deca.tree;

import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.ima.pseudocode.instructions.RTS;
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
        try {
            currentClass.getMembers().declare(methodName.getName(), methodDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("Method '" + methodName.getName() + "' is already defined in class '" + currentClass.getClass().getName() + "'.", getLocation()); 
        }

        methodName.setDefinition(methodDef);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    protected void codeGenMethod(DecacCompiler compiler) {
        // 1. On crée l'étiquette
        Label endLabel = new Label("end." + methodName.getName().getName());
        
        // 2. MODIFICATION : On l'enregistre dans le LabelManager
        compiler.getLabelManager().setMethodEndLabel(endLabel);

        // 3. Corps de la méthode
        this.methodBody.codeGenMethodBody(compiler);

        // 4. Le piège pour le return manquant
        if (!returnType.getType().isVoid()) {
            Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(
                    fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType.NO_RETURN);
            compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.BRA(errorLabel));
        }

        // 5. Fin de la méthode
        compiler.addLabel(endLabel);
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.RTS());
    }
}