package fr.ensimag.deca.tree;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.DecacCompiler;

public class TestReturn {

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

    // TODO: Add a test for a int to float return conversion once methods are implemented
}
