package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.VTableLayout;
import fr.ensimag.deca.codegen.VTableManager;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;
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
        if (superClassName != null) {
            Type superType = superClassName.verifyType(compiler);
                    
            if (!(superType.isClass())) { 
                // this should not happen if pass 1 was successful
                throw new ContextualError("Superclass " + superClassName.getName() + " of class " + className.getName() + " is not a class type.", superClassName.getLocation());
            }
        }
        
        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(className.getName());

        fields.verifyListFieldMembers(compiler, classDef);
        methods.verifyListMethodMembers(compiler, classDef);

    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(className.getName());
        EnvironmentExp localEnv = classDef.getMembers();

        fields.verifyListFieldRValue(compiler, localEnv, classDef);
        methods.verifyListMethodBody(compiler, classDef);
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

    @Override
    protected void codeGenVTable(DecacCompiler compiler) {
        VTableManager vm = compiler.getVTableManager();
        ClassDefinition classDef = className.getClassDefinition();
        ClassDefinition superClassDef = classDef.getSuperClass();

        VTableLayout layout = vm.reserveLayout(classDef, superClassDef);
        VTableLayout superLayout = vm.getClassLayout(superClassDef);

        fields.codeGenListFieldOffsets(classDef);
        methods.codeGenListVTable(compiler, layout, className);
        vm.updateLayoutGBOffset(layout);

        compiler.addComment("Code de la table des méthodes de " + className.getName());
        // store superclass VTable address
        int offset = superLayout.getGBOffset();
        DAddr addr = new RegisterOffset(offset, Register.GB);
        compiler.addInstruction(new LEA(addr, GPRegister.R0)); 
        DAddr vtableAddr = new RegisterOffset(layout.getGBOffset(), Register.GB);
        compiler.addInstruction(new STORE(GPRegister.R0, vtableAddr)); 

        // store methods addresses
        for (int index = 0; index < layout.getSize(); index++) {
            String label = layout.getLabel(index);
            LOAD inst = new LOAD(new LabelOperand(new Label(label)), GPRegister.R0);

            String[] split = label.split("\\.");
            String origin = split[1];
            String methodName = split[2];

            if (label.contains(className.getName().getName())){
                if (index < superLayout.getSize()) {
                        inst.setComment("Redéfinition de la méthode " + methodName);
                }
            }
            else {
                inst.setComment("Héritage de la méthode " + methodName + " de " + origin);
            }
            

            compiler.addInstruction(inst);
            DAddr methodAddr = new RegisterOffset(layout.getGBOffset() + 1 + index, Register.GB);
            compiler.addInstruction(new STORE(GPRegister.R0, methodAddr));
        }
            
    }

    @Override
	protected void codeGenInit(DecacCompiler compiler) {
        throw new UnsupportedOperationException("Not implemented yet."); 
    }

    @Override
	protected void codeGenMethods(DecacCompiler compiler) {
        throw new UnsupportedOperationException("Not implemented yet."); 
    }
}
