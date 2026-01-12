package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ClassDefinition;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * @author gl36
 * @date 11/01/2026
 */
public class DeclClass extends AbstractDeclClass {

    private final AbstractIdentifier className;
    private final AbstractIdentifier superClassName;
    private final ListDeclField fields;
    private final ListDeclMethod methods;

    public DeclClass(AbstractIdentifier className, AbstractIdentifier superClassName, ListDeclField fields, ListDeclMethod methods) {
        Validate.notNull(className);
        Validate.notNull(fields);
        Validate.notNull(methods);

        this.className = className;
        this.superClassName = superClassName;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        className.decompile(s);
        if (superClassName != null) {
            s.print(" extends ");
            superClassName.decompile(s);
        }
        if (!fields.getList().isEmpty() || !methods.getList().isEmpty()) {
            s.println(" {");
            s.indent();
            fields.decompile(s);
            methods.decompile(s);
            s.unindent();
            s.println("}");
        } else {
            s.println(" {}");
        }
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        Symbol classSymb = className.getName();
        ClassDefinition superClassDef;

        if (superClassName == null) {
            superClassDef = compiler.environmentType.OBJECT.getDefinition();
        } else {
            Symbol superClassSymb = superClassName.getName();
            TypeDefinition def = compiler.environmentType.defOfType(superClassSymb);

            if (def == null) {
                throw new ContextualError("Superclass " + superClassSymb.getName() + " of class " + classSymb.getName() + " is not defined.", superClassName.getLocation());
            }
            if (!def.getType().isClass()) {
                throw new ContextualError("Superclass " + superClassSymb.getName() + " of class " + classSymb.getName() + " is not a class type.", superClassName.getLocation());
            }

            superClassDef = (ClassDefinition) def;

            // set the type and definition of the SuperClassName identifier 
            // to be able to reference it later using super.            
            superClassName.setType(superClassDef.getType());
            superClassName.setDefinition(superClassDef);
        }

        ClassType classType = new ClassType(classSymb, className.getLocation(), superClassDef);
        ClassDefinition classDef = classType.getDefinition();
        
        compiler.environmentType.declare(classSymb, classDef); // throws ContextualError if already defined

        className.setDefinition(classDef);
        className.setType(classType);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, false);
        if (superClassName != null) {
            superClassName.prettyPrint(s, prefix, false);
        }
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
        if (superClassName != null) {
            superClassName.iter(f);
        }
        fields.iter(f);
        methods.iter(f);
    }

}
