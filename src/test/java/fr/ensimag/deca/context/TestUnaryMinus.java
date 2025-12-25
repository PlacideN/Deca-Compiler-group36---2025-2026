package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.UnaryMinus;
import fr.ensimag.deca.DecacCompiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;



public class TestUnaryMinus {
    @Mock
    AbstractExpr intexpr;
    @Mock
    AbstractExpr floatexpr;
    @Mock 
    AbstractExpr boolexpr;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null); 

        Type INT = compiler.environmentType.INT;
        Type FLOAT = compiler.environmentType.FLOAT;
        Type BOOLEAN = compiler.environmentType.BOOLEAN;

        when(intexpr.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(boolexpr.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
    }

    @Test
    public void testUnaryMinusOnInt() throws ContextualError {
        UnaryMinus t = new UnaryMinus(intexpr);
        assertTrue(t.verifyExpr(compiler, null, null).isInt());

        verify(intexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testUnaryMinusOnFloat() throws ContextualError {
        UnaryMinus t = new UnaryMinus(floatexpr);
        assertTrue(t.verifyExpr(compiler, null, null).isFloat());

        verify(floatexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testUnaryMinusOnInvalidType() throws ContextualError {
        UnaryMinus t = new UnaryMinus(boolexpr);
        assertThrows(
            ContextualError.class,
            () -> t.verifyExpr(compiler, null, null),
            "Expected ContextualError for UnaryMinus on non-numeric type (boolean)"
        );

        verify(boolexpr).verifyExpr(compiler, null, null);
    }

}
