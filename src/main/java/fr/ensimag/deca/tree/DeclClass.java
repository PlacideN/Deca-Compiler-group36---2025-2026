package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.VTableLayout;
import fr.ensimag.deca.codegen.VTableManager;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
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
        
        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(className.getName());

        if (superClassName != null) {
            Type superType = superClassName.verifyType(compiler);

            if (!(superType.isClass())) { 
                throw new ContextualError("Superclass " + superClassName.getName() + " is not a class type.", superClassName.getLocation());
            }

            ClassDefinition superClassDef = (ClassDefinition) compiler.environmentType.defOfType(superClassName.getName());
            
            classDef.setNumberOfFields(superClassDef.getNumberOfFields());
            classDef.setNumberOfMethods(superClassDef.getNumberOfMethods());
        } else {
            classDef.setNumberOfFields(0);
            classDef.setNumberOfMethods(0);
        }

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
        classDef.setVTableOffset(layout.getGBOffset());
        VTableLayout superLayout = vm.getClassLayout(superClassDef);

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
        String name = className.getName().getName();
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("              Classe " + name +"               ");
        compiler.addComment("--------------------------------------------------");
        Label initLabel = compiler.getLabelManager().getInitLabel(name);
        compiler.addComment("---------- Initialisation des champs de " + name);
        compiler.addLabel(initLabel);

        IMAProgram program = compiler.getProgram();
        IMAProgram block = new IMAProgram();

        compiler.setProgram(block);
        compiler.getRegisterManager().resetMaxTempStackUsage();

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
        
        // init fields from superclass
        ClassDefinition superClassDef = className.getClassDefinition().getSuperClass();
        if (superClassDef != null && !superClassDef.getType().isObjectClass()) {
            compiler.addInstruction(new PUSH(Register.R1));
            Label superInitLabel = compiler.getLabelManager().getInitLabel(superClassDef.getType().getName().getName());
            compiler.addInstruction(new BSR(superInitLabel));
            compiler.addInstruction(new POP(Register.R1));
        }
        
        fields.codeGenListDefaultInits(compiler); // initialize fields to default values
        fields.codeGenListExplicitInits(compiler);
        compiler.addInstruction(new RTS());

        compiler.setProgram(program);
        int maxStackUsage = compiler.getRegisterManager().getMaxTempStackUsage();
        if (superClassDef != null && !superClassDef.getType().isObjectClass()) {
            maxStackUsage += 2; // PUSH and SUBSP for superclass init
        }

        if (!compiler.getCompilerOptions().getNoCheck() && maxStackUsage > 0) {
            compiler.addInstruction(new TSTO(maxStackUsage));
            Label errLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.STACK_OVERFLOW);
            compiler.addInstruction(new BOV(errLabel));
        }
        program.append(block);
    }

    @Override
	protected void codeGenMethods(DecacCompiler compiler) {
        methods.codeGenListMethods(compiler);
    }
}
