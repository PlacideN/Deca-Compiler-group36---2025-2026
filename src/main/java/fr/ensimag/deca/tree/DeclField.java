package fr.ensimag.deca.tree;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

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
            s.print(" = ");
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void verifyFieldRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
