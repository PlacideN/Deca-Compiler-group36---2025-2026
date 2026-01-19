package fr.ensimag.deca.tree;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;

public class TestReturn {

    DecacCompiler compiler;

    @BeforeEach
    public void setup() {
        compiler = new DecacCompiler(null, null);
    }

    @Test
    public void testVoidReturnInMain() {
        DecacCompiler compiler = new DecacCompiler(null, null);
        // return 1;
        Return returnInst = new Return(new IntLiteral(1));

        // In main, verifyInst passes a void return type regardless of the specified 
        // return attribute and should throw ContextualError 
        assertThrows(
            ContextualError.class, 
            () -> returnInst.verifyInst(compiler, null, null, compiler.environmentType.VOID),
            "Should throw ContextualError for using return in main"
        );
    }

    @Test
    public void testIntToFloatReturnConversion() throws ContextualError {
        AbstractExpr intExpr = new IntLiteral(1);
        Return returnInst = new Return(intExpr);
        Type expectedType = compiler.environmentType.FLOAT;

        returnInst.verifyInst(compiler, null, null, expectedType);

        assertTrue(returnInst.getExpression() instanceof ConvFloat);
    }
}
