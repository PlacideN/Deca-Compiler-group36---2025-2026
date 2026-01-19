package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.NotEquals;
import fr.ensimag.deca.tree.Equals;
import fr.ensimag.deca.tree.Lower;
import fr.ensimag.deca.tree.LowerOrEqual;
import fr.ensimag.deca.tree.Greater;
import fr.ensimag.deca.tree.GreaterOrEqual;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.AbstractOpCmp;
import fr.ensimag.deca.tree.ConvFloat;
import fr.ensimag.deca.DecacCompiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;
import java.util.function.BiFunction;

public class TestOpComp {

    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;
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

        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(boolexpr.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
    }

    @ParameterizedTest(name = "{1} using (int, int)")
    @MethodSource("allOpCompProvider")
    public void testIntInt(BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp> opCompConstructor, String opName) throws ContextualError {
        AbstractOpCmp t = opCompConstructor.apply(intexpr1, intexpr2);
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean(), "FAILED at operation: " + opName);

        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(intexpr2).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (float, int)")
    @MethodSource("allOpCompProvider")
    public void testFloatInt(BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp> opCompConstructor, String opName) throws ContextualError {
        AbstractOpCmp t = opCompConstructor.apply(floatexpr, intexpr1);
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean(), "FAILED at operation: " + opName);

        // ConvFloat should have been inserted on the right side
        assertFalse(t.getLeftOperand() instanceof ConvFloat, "FAILED at operation: " + opName);
        assertTrue(t.getRightOperand() instanceof ConvFloat, "FAILED at operation: " + opName);

        verify(floatexpr).verifyExpr(compiler, null, null);
        verify(intexpr1).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (int, float)")
    @MethodSource("allOpCompProvider")
    public void testIntFloat(BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp> opCompConstructor, String opName) throws ContextualError {
        AbstractOpCmp t = opCompConstructor.apply(intexpr1, floatexpr);
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean(), "FAILED at operation: " + opName);
        // ConvFloat should have been inserted on the left side
        assertTrue(t.getLeftOperand() instanceof ConvFloat, "FAILED at operation: " + opName);
        assertFalse(t.getRightOperand() instanceof ConvFloat, "FAILED at operation: " + opName);

        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr).verifyExpr(compiler, null, null);
    }

    @ParameterizedTest(name = "{1} using (bool, int)")
    @MethodSource("allOpCompProvider")
    public void testBoolInt(BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp> opCompConstructor, String opName) throws ContextualError {
        AbstractOpCmp t = opCompConstructor.apply(boolexpr, intexpr1);
        assertThrows(
            ContextualError.class,
            () -> t.verifyExpr(compiler, null, null),
            "Expected ContextualError for Comparison operation with incorrect type (bool, int)"
        );
    
        verify(boolexpr).verifyExpr(compiler, null, null);
        verify(intexpr1).verifyExpr(compiler, null, null);
    }


    // All comparison operators follow the same typing rules (arithmetic operations types)
    public static Stream<Arguments> allOpCompProvider() {
        return Stream.concat(
            opExactCmpProvider(),
            opIneqCmpProvider()
        );
    }

    // Use this as a MethodSource to implement Class and Null (non)equality tests 
    public static Stream<Arguments> opExactCmpProvider() {
        return Stream.of(
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp>) Equals::new, "Equals"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp>) NotEquals::new, "NotEquals")
        );
    }

    public static Stream<Arguments> opIneqCmpProvider() {
        return Stream.of(
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp>) Lower::new, "Lower"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp>) LowerOrEqual::new, "LowerOrEqual"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp>) Greater::new, "Greater"),
            Arguments.of((BiFunction<AbstractExpr, AbstractExpr, AbstractOpCmp>) GreaterOrEqual::new, "GreaterOrEqual")
        );        
    }
}