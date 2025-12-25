package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Not;
import fr.ensimag.deca.DecacCompiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;


public class TestNot {
    @Mock
    AbstractExpr boolexpr;
    @Mock
    AbstractExpr intexpr;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);   

        Type BOOLEAN = compiler.environmentType.BOOLEAN;
        Type INT = compiler.environmentType.INT;

        when(boolexpr.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(intexpr.verifyExpr(compiler, null, null)).thenReturn(INT);
    }

    @Test
    public void testNotOnBoolean() throws ContextualError {
        Not t = new Not(boolexpr);
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        verify(boolexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testNotOnInvalidType() throws ContextualError {
        Not t = new Not(intexpr);
        assertThrows(
            ContextualError.class,
            () -> t.verifyExpr(compiler, null, null),
            "Expected ContextualError for Not on non-boolean type (int)"
        );
        verify(intexpr).verifyExpr(compiler, null, null);
    }
    
}
