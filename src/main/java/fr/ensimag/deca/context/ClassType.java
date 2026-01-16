package fr.ensimag.deca.context;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import org.apache.commons.lang.Validate;

/**
 * Type defined by a class.
 *
 * @author gl36
 * @date 01/01/2026
 */
public class ClassType extends Type {
    
    protected ClassDefinition definition;
    
    public ClassDefinition getDefinition() {
        return this.definition;
    }
            
    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
        this.definition = new ClassDefinition(this, location, superClass);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }
    
    /**
     * Compares this class type with another type.
     */
    @Override
    public boolean sameType(Type otherType) {
        if (!otherType.isClass()) {
            return false;
        }
        try {
            ClassType otherClassType = otherType.asClassType("Internal error: not a class type.", null);
            return this.getName().equals(otherClassType.getName());
        } catch (ContextualError e) {
            throw new fr.ensimag.deca.tools.DecacInternalError("Should not happen: isClass() was true but asClassType failed.");
        }
    }

    /**
     * Return true if potentialSuperClass is a superclass of this class.
     */
    public boolean isSubClassOf(ClassType potentialSuperClass) {
        if (this.sameType(potentialSuperClass)) {
            return true;
        }
        ClassDefinition superClassDef = this.getDefinition().getSuperClass(); // on accède directement au parent de notre classe
        if (superClassDef == null){
            return false;
        }
        return superClassDef.getType().isSubClassOf(potentialSuperClass);
        
    }


}
