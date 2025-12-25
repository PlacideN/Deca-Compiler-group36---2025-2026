package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Modulo;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class TestModulo {
    
    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;
    @Mock
    AbstractExpr floatexpr;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);  // init mocks is deprecated
        compiler = new DecacCompiler(null, null); // creates the default type environment

        // Use default types (which already create a Symbol instance) to ensure toString() 
        // works when a ContextualError is thrown
        Type INT = compiler.environmentType.INT; 
        Type FLOAT = compiler.environmentType.FLOAT;

        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
    }

    @Test
    public void testIntInt() throws ContextualError {
        Modulo t = new Modulo(intexpr1, intexpr2);
        assertTrue(t.verifyExpr(compiler, null, null).isInt());
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(intexpr2).verifyExpr(compiler, null, null);  
    }

    @Test
    public void testIntFloat() throws ContextualError {
        Modulo t = new Modulo(intexpr1, floatexpr);
        assertThrows(ContextualError.class, () -> { t.verifyExpr(compiler, null, null);}, "Expected ContextualError for Modulo with incorrect type (int, float)");

        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr).verifyExpr(compiler, null, null);
    }
}