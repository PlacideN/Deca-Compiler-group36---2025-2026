package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;


import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;


public class TestInitialization {

    @Mock
    EnvironmentExp env;

    AbstractExpr intexpr;
    DeclVar declVar;
    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        // create a spy of IntLiteral because a mock would throw NullPointerException
        // when calling expression.verifyRValue because this.expression is null in a mock.
        // Since it is the method we want to test, we want to call the real method 
        // and not mock its behavior.
        intexpr = spy(new IntLiteral(1));

        // use doReturn (with spies) to mock verifyExpr non-void method because 
        // it doesn't execute the real verifyExpr method which is implemented 
        // but not relevant for this unit test
        doReturn(compiler.environmentType.INT).when(intexpr).verifyExpr(compiler, env, null);

    }
    
    @Test
    public void testNoInitialization() {
        AbstractInitialization noInit = new NoInitialization();

        assertDoesNotThrow(() -> {
            noInit.verifyInitialization(compiler, compiler.environmentType.INT, env, null);
        });

        // verifyExpr is not called (NoInitialization has no expression)
    }

    @Test
    public void TestInitializationCompatible() throws ContextualError {
        AbstractInitialization init = new Initialization(intexpr);

        init.verifyInitialization(compiler, compiler.environmentType.INT, env, null);

        verify(intexpr).verifyRValue(compiler, env, null, compiler.environmentType.INT);
    }

    @Test
    public void TestInitializationIncompatible() throws ContextualError {
        AbstractInitialization init = new Initialization(intexpr);
        assertThrows(
            ContextualError.class, 
            () -> init.verifyInitialization(compiler, compiler.environmentType.BOOLEAN, env, null),
            "Should throw ContextualError if RValue (initialization) and LValue (variable) have incompatible types"
        );
    }

    @Test
    public void TestInitializationWithFloat() throws ContextualError{
        Initialization init = new Initialization(intexpr);

        init.verifyInitialization(compiler, compiler.environmentType.FLOAT, env, null);

        verify(intexpr).verifyRValue(compiler, env, null, compiler.environmentType.FLOAT);
        assertTrue(init.getExpression() instanceof ConvFloat);
        assertEquals(compiler.environmentType.FLOAT, init.getExpression().getType());
    }

}
