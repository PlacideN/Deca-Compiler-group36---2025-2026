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
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
}
