package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.util.HashMap;
import java.util.Map;

public class VTableManager {
    private int nextGBSlot = 1;
    private final Map<ClassDefinition, VTableLayout> classLayouts = new HashMap<>();
    private final DecacCompiler compiler;

    public VTableManager(DecacCompiler compiler) {
        this.compiler = compiler;
    }

    public VTableLayout reserveLayout(ClassDefinition classDef, ClassDefinition superClassDef) {
        VTableLayout layout = new VTableLayout(nextGBSlot);

        if (superClassDef != null) {
            layout.copyLayout(classLayouts.get(superClassDef)); // inherit methods
        } else {
            layout.addMethod("equals", compiler.getLabelManager().getObjectEqualsLabel().toString()); // inherit from Object
        }

        classLayouts.put(classDef, layout);
        return layout;
    }

    public void updateLayoutGBOffset(VTableLayout layout) {
        nextGBSlot = layout.getGBOffset() + 1 + layout.getSize(); // one slot is for superclass vtable address
    }

    public int getNextGBSlot() {
        return nextGBSlot;
    }

    
    public VTableLayout getClassLayout(ClassDefinition classDef) {
        return classLayouts.get(classDef);
    }

    public void codeGenObjectVTable(DecacCompiler compiler) {
        ClassDefinition objDef = compiler.environmentType.OBJECT.getDefinition();

        VTableLayout objLayout = reserveLayout(objDef, null);
        // no methods to add for Object
        updateLayoutGBOffset(objLayout);
        int base = objLayout.getGBOffset();

        compiler.addComment("Code de la table des méthodes de Object");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(base, Register.GB)));
        compiler.addInstruction(new LOAD(new LabelOperand(compiler.getLabelManager().getObjectEqualsLabel()), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(base + 1, Register.GB)));
    }
}
