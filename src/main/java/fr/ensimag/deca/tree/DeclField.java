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
        if (visibility == Visibility.PUBLIC) {
            s.print("public ");
        } else if (visibility == Visibility.PROTECTED) {
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

        // declaration
        int index = currentClass.getNumberOfFields() + 1;
        FieldDefinition fieldDef = new FieldDefinition(fieldType, getLocation(), visibility, currentClass.getSuperClass(), index);
        try {
            currentClass.getMembers().declare(name, fieldDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("Field " + name + " already defined in class", getLocation());
        }

        currentClass.setNumberOfFields(index);
        
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
}
