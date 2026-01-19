package fr.ensimag.deca.tree;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * @author gl36
 * @date 11/01/2026
 */
public class DeclField extends AbstractDeclField {
    private final Visibility visibility;
    private final AbstractIdentifier type;
    private final AbstractIdentifier fieldName;
    private final AbstractInitialization init;

    public DeclField(Visibility visibility, AbstractIdentifier type, AbstractIdentifier fieldName, AbstractInitialization init) {
        Validate.notNull(visibility);
        Validate.notNull(type);
        Validate.notNull(fieldName);
        Validate.notNull(init); // when not initialized the param is an instance of NoInitialization

        this.visibility = visibility;
        this.type = type;
        this.fieldName = fieldName;
        this.init = init;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (visibility == Visibility.PROTECTED) {
            s.print("protected ");
        }
        type.decompile(s);
        s.print(" ");
        fieldName.decompile(s);
        if (init != null) {
            init.decompile(s);
        }
        s.println(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        if (init != null) {
            init.prettyPrint(s, prefix, true);
        }
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        if (init != null) {
            init.iter(f);
        }
    }

    @Override
    protected void verifyFieldMembers(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        Type fieldType = type.verifyType(compiler);
        if (fieldType.isVoid()) {
            throw new ContextualError("Field type cannot be void", getLocation());
        }

        // name collision
        Symbol name = fieldName.getName();
        ExpDefinition inherited = currentClass.getSuperClass().getMembers().get(name);
        if (inherited != null && inherited.isMethod()) {
            throw new ContextualError("Field name collides with an inherited method", getLocation());
        }

        
        // first field is at 1 (0 is VTable) then add superclass fields
        int index = currentClass.getNumberOfFields() + 1;

        currentClass.setNumberOfFields(index);


        FieldDefinition fieldDef = new FieldDefinition(fieldType, getLocation(), visibility, currentClass, index);
        fieldDef.setOperand(new RegisterOffset(index, Register.R1));
        
        try {
            currentClass.getMembers().declare(name, fieldDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("Field " + name + " already defined in class", getLocation());
        }

        
        fieldName.setDefinition(fieldDef);
        fieldName.setType(fieldType);
    }

    @Override
    protected void verifyFieldRValue(DecacCompiler compiler, ClassDefinition currentClass) 
            throws ContextualError {
        FieldDefinition fieldDef = (FieldDefinition) fieldName.getFieldDefinition();
        Type fieldType = fieldDef.getType();
        EnvironmentExp env = currentClass.getMembers();
        init.verifyInitialization(compiler, fieldType, env, currentClass);
    }

    @Override
    protected void codeGenDefaultInit(DecacCompiler compiler) {
        FieldDefinition fieldDef = fieldName.getFieldDefinition();
        Type fieldType = fieldDef.getType();
        String className = fieldDef.getContainingClass().getType().getName().getName();
        String name = fieldName.getName().getName();

        compiler.addComment("Initialisation de " + className + "." + name);
        if (fieldType.isFloat()) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0.0f), Register.R0));
        } else if (fieldType.isClassOrNull()) {
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        } else {
            compiler.addInstruction(new LOAD(0, Register.R0));
        }
        compiler.addInstruction(new STORE(Register.R0, fieldDef.getOperand()));        
    }

    @Override
    protected void codeGenExplicitInit(DecacCompiler compiler) {
        // only if there is an explicit initialization
        if (init instanceof Initialization) { 
            FieldDefinition fieldDef = fieldName.getFieldDefinition();
            String className = fieldDef.getContainingClass().getType().getName().getName();
            String name = fieldName.getName().getName();
            compiler.addComment("Initialisation explicite de " + className + "." + name);
            init.codeGenInit(compiler, fieldDef.getOperand());
        }
    }
}
