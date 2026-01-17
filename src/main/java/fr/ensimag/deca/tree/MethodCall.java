package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;



import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;


public class MethodCall extends AbstractExpr {
    private final AbstractExpr object;
    private final AbstractIdentifier methodName;
    private final ListExpr params;

    public MethodCall(AbstractExpr object, AbstractIdentifier methodName, ListExpr params) {
        Validate.notNull(object);
        Validate.notNull(methodName);
        Validate.notNull(params);
        
        this.object = object;
        this.methodName = methodName;
        this.params = params;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        s.print(".");
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        methodName.iter(f);
        params.iter(f);
    }

    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, 
            ClassDefinition currentClass) throws ContextualError {
        
        Type type = object.verifyExpr(compiler, localEnv, currentClass);
        ClassType classType = type.asClassType("Method call must be on a class instance", getLocation());
        
        EnvironmentExp envExp = classType.getDefinition().getMembers();
        MethodDefinition methodDef = methodName.verifyMethodIdent(compiler, envExp);
        
        Signature sig = methodDef.getSignature();
        params.verifyListExprSignature(compiler, localEnv, currentClass, sig);

        // AST decorations
        methodName.setDefinition(methodDef);
        this.setType(methodDef.getType());
        
        return methodDef.getType();
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {

        int nombre_params = params.size();
        compiler.addInstruction( new ADDSP(nombre_params+1)); //+1 pour le this

        int i = 0;
        for (AbstractExpr param : params.getList()) { 
            // pour chaque paramètres, on évalue le paramètre dans target
            param.codeGenExpr(compiler, target);
            // on le stocke dans la pile
            compiler.addInstruction( new STORE(target, new RegisterOffset(-(nombre_params - 1 - i), GPRegister.SP)));
            i++;
        }
        // on évalue l'objet à gauche dans target
        object.codeGenExpr(compiler, target);

        // vérification de la non-nullité de l'objet
        compiler.addInstruction( new CMP(new NullOperand(), target));
        compiler.addInstruction( new BEQ(compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.NULL_DEREFERENCE)));

        // il ne reste plus qu'a stocker this en debut de la pile
        compiler.addInstruction( new STORE(target, new RegisterOffset(-nombre_params, GPRegister.SP)));


        // On charge l'adresse de la VTable (située à l'offset 0 de l'objet) dans target
        compiler.addInstruction(new LOAD(new RegisterOffset(0, target), target));

        // puis on appelle la méthode (située à l'offset methodOffset dans la VTable)
        int methodOffset = ((MethodDefinition) methodName.getDefinition()).getIndex();
        compiler.addInstruction( new BSR(new RegisterOffset(methodOffset, target)));

        // On nettoie la pile après l'appel
        compiler.addInstruction( new SUBSP(nombre_params + 1));

        //Enfin on stocke le résultat de la méthode
        compiler.addInstruction( new LOAD(GPRegister.R0, target));



    }

    
    
}
