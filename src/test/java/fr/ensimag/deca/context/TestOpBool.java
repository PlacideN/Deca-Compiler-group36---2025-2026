package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.And;
import fr.ensimag.deca.tree.Or;
import fr.ensimag.deca.tree.Equals;
import fr.ensimag.deca.tree.NotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Test for boolean operations (and, or, equals, not equals) using mockito.
 * Only boolean types are allowed for these operations.
 */
public class TestOpBool {

    @Mock
    AbstractExpr boolexpr1;
    @Mock
    AbstractExpr boolexpr2;
    @Mock
    AbstractExpr intexpr;

    DecacCompiler compiler;

    @BeforeEach
    public void setUp() throws ContextualError {
        MockitoAnnotations.openMocks(this); 
        compiler = new DecacCompiler(null, null);

        Type BOOLEAN = compiler.environmentType.BOOLEAN;
        Type INT = compiler.environmentType.INT;

        when(boolexpr1.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(boolexpr2.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(intexpr.verifyExpr(compiler, null, null)).thenReturn(INT);        
    }

    @ParameterizedTest(name = "{1} using (bool, bool)")
    @MethodSource("opBoolProvider")
    public void testBoolBool(BiFunction<AbstractExpr, AbstractExpr, AbstractExpr> opBoolConstructor, String opName) throws ContextualError {
        AbstractExpr t = opBoolConstructor.apply(boolexpr1, boolexpr2);
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean(), "FAILED at operation: " + opName);

        // Verify mocks
        verify(boolexpr1, times(1)).verifyExpr(compiler, null, null);
        verify(boolexpr2, times(1)).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (bool, int)")
    @MethodSource("opBoolProvider")
    public void testBoolInt(BiFunction<AbstractExpr, AbstractExpr, AbstractExpr> opBoolConstructor, String opName) throws ContextualError {
        AbstractExpr t = opBoolConstructor.apply(boolexpr1, intexpr);
        assertThrows(
            ContextualError.class,
            () -> t.verifyExpr(compiler, null, null),
            "Expected ContextualError for Boolean operation with incorrect type (int, float)"
        );

        // Verify mocks
        verify(boolexpr1, times(1)).verifyExpr(compiler, null, null);
        verify(intexpr, times(1)).verifyExpr(compiler, null, null);
    }

    public static Stream<Arguments> opBoolProvider() {
        return Stream.of(
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractExpr>) And::new, "And"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractExpr>) Or::new, "Or"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractExpr>) Equals::new, "Equals"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractExpr>) NotEquals::new, "NotEquals")
        );
    }
    
}
