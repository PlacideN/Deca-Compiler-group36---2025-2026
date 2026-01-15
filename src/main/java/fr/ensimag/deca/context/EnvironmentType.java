package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.Map;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl36
 * @date 01/01/2026
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {
        
        envTypes = new HashMap<Symbol, TypeDefinition>();
        
        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.
        
        Symbol nullSymb = compiler.createSymbol("null");
        NULL = new NullType(nullSymb);
        // not visible for the user

        Symbol objectSymb = compiler.createSymbol("Object");
        OBJECT = new ClassType(objectSymb, Location.BUILTIN, null);
        initializeObjectClass(compiler);
        envTypes.put(objectSymb, OBJECT.getDefinition());
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    public void declare(Symbol s, TypeDefinition def) throws ContextualError {
        if (envTypes.containsKey(s)) {
            throw new ContextualError("Type " + s + " is already defined.", def.getLocation());
        }
        envTypes.put(s, def);
    }

    private void initializeObjectClass(DecacCompiler compiler) {
        // add Equals method to Object class
        Signature equalsSignature = new Signature();
        Symbol equalsSymb = compiler.createSymbol("equals");
        equalsSignature.add(OBJECT); // parameter of type Object
        MethodDefinition equalsMethod = new MethodDefinition(
            BOOLEAN,
            Location.BUILTIN,
            equalsSignature,
            0
        );

        try {
            OBJECT.getDefinition().getMembers().declare(equalsSymb, equalsMethod);
        } catch (EnvironmentExp.DoubleDefException e) { // should not happen
            throw new DecacInternalError("Double definition of method equals in Object class");
        }

        OBJECT.getDefinition().setNumberOfMethods(1);
    }

    /**
     * Implements the type_arith_op(T1, T2) rule.
     * <pre>
     * (int, int)     -> int
     * (int, float)   -> float
     * (float, int)   -> float
     * (float, float) -> float
     * </pre>
     * @param t1 Type of the left operand
     * @param t2 Type of the right operand
     * @return The resulting numeric Type, or null if types are not numeric.
     */
    public Type typeArithOp(Type t1, Type t2) {
        if (t1.isInt() && t2.isInt()) {
            return INT;
        }
        if (t1.isNumeric() && t2.isNumeric()) {
            return FLOAT;
        }
        return null;
    }

    /**
     * Implements assign_compatible(T1, T2) rule.
     * Check if a value of type T2 can be assigned to a variable of type T1 
     * (either float promotion or subtype).
     */
    public boolean assignCompatible(Type t1, Type t2) {
        if (t1.isFloat() && t2.isInt()) { // Float promotion
            return true;
        }

        return subtype(t2, t1);
    }

    /**
     * Implements cast_compatible(T1, T2) rule.
     * Check if a value of type T1 can be cast to type T2
     */
    public boolean castCompatible(Type t1, Type t2) {
        if (t1.isVoid() || t2.isVoid()) {
            return false;
        }

        if ((t1.isFloat() && t2.isInt()) || (t1.isInt() && t2.isFloat())) {
            return true;
        }
        return subtype(t1, t2) || subtype(t2, t1);
    }

    /**
     * Implements the subtype(T1, T2) rule.
     * Check if T1 is a subtype of T2.
     */   
    public boolean subtype(Type t1, Type t2) {
        if (t1.equals(t2)) { // same type
            return true;
        }

        if (t1.isNull() && t2.isClass()) { // null is subtype of all class types
            return true;
        }

        if (t1.isClass() && t2.isClass()) { // inheritance
            ClassDefinition superClassDef = ((ClassType) t1).getDefinition().getSuperClass();
            if (superClassDef == null) { // base case => reached Object class
                return false;
            }
            return subtype(superClassDef.getType(), t2); // recursive call
        }
        
        return false;
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
    public final NullType    NULL;
    public final ClassType  OBJECT;
}
