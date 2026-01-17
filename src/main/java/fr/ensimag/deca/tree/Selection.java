package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FieldDefinition;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.RegisterOffset;




/**
 * @author gl36
 * @date 12/01/2026
 */
public class Selection extends AbstractLValue {
    private final AbstractExpr object;
    private final AbstractIdentifier fieldName;

    public Selection(AbstractExpr object, AbstractIdentifier fieldName) {
        Validate.notNull(object);
        Validate.notNull(fieldName);
        this.object = object;
        this.fieldName = fieldName;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        s.print(".");
        fieldName.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        fieldName.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, true);
    }

    @Override
    public ExpDefinition getExpDefinition() {
        return fieldName.getExpDefinition();
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, 
            ClassDefinition currentClass) throws ContextualError {
        
        Type type = object.verifyExpr(compiler, localEnv, currentClass);
        ClassType classType = type.asClassType("Selection must be on a class instance", getLocation());
        
        EnvironmentExp envExp = classType.getDefinition().getMembers();
        FieldDefinition fieldDef = fieldName.verifyFieldIdent(compiler, envExp);
        fieldName.setDefinition(fieldDef);
        
        if (fieldDef.getVisibility() == Visibility.PROTECTED) {
            if (currentClass == null) {
                throw new ContextualError("Protected field '" + fieldName.getName() + 
                    "' cannot be accessed in main", getLocation());
            }

            if (!compiler.environmentType.subtype(type, currentClass.getType())) {
                throw new ContextualError("Access to protected field '" + 
                    fieldName.getName() + "' is not allowed here. The object type (" + type + 
                    ") must be a subtype of the current class (" + currentClass.getType() + ")", 
                    getLocation());
            }
            
            if (!compiler.environmentType.subtype(currentClass.getType(), fieldDef.getContainingClass().getType())) {
                throw new ContextualError("Visibility error for protected field", getLocation());
            }
        }

        this.setType(fieldDef.getType());
        return fieldDef.getType();
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister target) {
        // on evalue l'expression à gauche, on a dans target l'adresse de l'objet
        object.codeGenExpr(compiler, target);

        //S'il est null, on saute vers une erreur
        compiler.addInstruction(new CMP(new NullOperand(),target) );
        compiler.addInstruction(new BEQ(compiler.getLabelManager().getNullDereferenceLabel()));

        // on recupere l'adresse du champ 
        int index = fieldName.getFieldDefinition().getIndex();

        // puis on charge la valeur du champ dans target
        compiler.addInstruction(new LOAD(new RegisterOffset(index, target), target));

        
    }

}
