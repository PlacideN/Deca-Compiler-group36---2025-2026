package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.AbstractOpArith;
import fr.ensimag.deca.tree.Plus;
import fr.ensimag.deca.tree.Minus;
import fr.ensimag.deca.tree.Multiply;
import fr.ensimag.deca.tree.Divide;
import fr.ensimag.deca.tree.ConvFloat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Test for arithmetic operations (plus, minus, multiply, divide) using mockito.
 * These operations follow the same unit tests for context checking as they
 * all have the same type requirements.
 */
public class TestOpArith {

    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);

    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;
    @Mock
    AbstractExpr floatexpr1;
    @Mock
    AbstractExpr floatexpr2;
    
    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this); // init mocks is deprecated
        compiler = new DecacCompiler(null, null);
        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr1.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(floatexpr2.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
    }

    @ParameterizedTest(name = "{1} using (int, int)")
    @MethodSource("opArithProvider")
    public void testIntInt(BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> opArithConstructor, String opName) throws ContextualError {

        AbstractOpArith t = opArithConstructor.apply(intexpr1, intexpr2);
        assertTrue(t.verifyExpr(compiler, null, null).isInt(), "FAILED at operation: " + opName);

        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(intexpr2).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (float, int)")
    @MethodSource("opArithProvider")
    public void testFloatInt(BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> opArithConstructor, String opName) throws ContextualError {
        AbstractOpArith t = opArithConstructor.apply(floatexpr1, intexpr1);
        assertTrue(t.verifyExpr(compiler, null, null).isFloat(), "FAILED at operation: " + opName);

        // ConvFloat should have been inserted on the right side
        assertFalse(t.getLeftOperand() instanceof ConvFloat, "FAILED at operation: " + opName);
        assertTrue(t.getRightOperand() instanceof ConvFloat, "FAILED at operation: " + opName);

        verify(floatexpr1).verifyExpr(compiler, null, null);
        verify(intexpr1).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (int, float)")
    @MethodSource("opArithProvider")
    public void testIntFloat(BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> opArithConstructor, String opName) throws ContextualError {
        AbstractOpArith t = opArithConstructor.apply(intexpr1, floatexpr1);
        assertTrue(t.verifyExpr(compiler, null, null).isFloat(), "FAILED at operation: " + opName);

        // ConvFloat should have been inserted on the left side
        assertTrue(t.getLeftOperand() instanceof ConvFloat, "FAILED at operation: " + opName);
        assertFalse(t.getRightOperand() instanceof ConvFloat, "FAILED at operation: " + opName);

        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (float, float)")
    @MethodSource("opArithProvider")
    public void testFloatFloat(BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> opArithConstructor, String opName) throws ContextualError {
        AbstractOpArith t = opArithConstructor.apply(floatexpr1, floatexpr2);
        assertTrue(t.verifyExpr(compiler, null, null).isFloat(), "FAILED at operation: " + opName);
        verify(floatexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr2).verifyExpr(compiler, null, null);
    }

    public static Stream<Arguments> opArithProvider(){
            // Create references to the instance constructors for each operation
            BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> plusRef = Plus::new;
            BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> minusRef = Minus::new;
            BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> multRef = Multiply::new;
            BiFunction<AbstractExpr, AbstractExpr, AbstractOpArith> divRef = Divide::new;

            // Return a Stream made of Arguments that contain the ref and the name of the operation
            return Stream.of(
                Arguments.of(plusRef, "Plus"), // Pass the name of the operation for easier debugging
                Arguments.of(minusRef, "Minus"),
                Arguments.of(multRef, "Multiply"),
                Arguments.of(divRef, "Divide")
            );
        }
    
}

