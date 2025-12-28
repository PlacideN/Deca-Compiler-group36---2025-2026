package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.Map;
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
        
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
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
     * Implements the subtype(T1, T2) rule.
     * Check if T1 is a subtype of T2.
     */   
    public boolean subtype(Type t1, Type t2) {
        // Add other rules when classes are implemented
        return t1.sameType(t2);
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
}
