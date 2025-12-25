package fr.ensimag.deca.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;

public class TestEnvironmentExp {
    EnvironmentExp child;
    EnvironmentExp parent;

    Symbol x;
    ExpDefinition def1;
    ExpDefinition def2;

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() {
        compiler = new DecacCompiler(null, null);
        x = compiler.createSymbol("x");

        def1 = new VariableDefinition(null, null);
        def2 = new VariableDefinition(null, null);

        parent = new EnvironmentExp(null);
        child = new EnvironmentExp(parent);
    }

    @Test
    public void testDeclareAndLookup() throws DoubleDefException {
        parent.declare(x, def1);   
        assertEquals(def1, parent.get(x));
    }

    @Test
    public void testLookupInParent() throws DoubleDefException {
        parent.declare(x, def1);   
        // x is not declared in child, but the child should find it in parent
        assertEquals(def1, child.get(x));
    }

    @Test
    public void testChildPriority() throws DoubleDefException {
        parent.declare(x, def1);   
        child.declare(x, def2);   
        // x is declared in both child and parent, child definition should take priority
        assertEquals(def2, child.get(x));
    }

    @Test
    public void testDoubleDeclaration() throws DoubleDefException {
        child.declare(x, def1);
        assertThrows(
            DoubleDefException.class, 
            () -> child.declare(x, def2),
            "Expected DoubleDefException when declaring the same symbol twice in the same environment"
        );
    }
}