package fr.ensimag.deca.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

public class TestNoOperation {
    DecacCompiler compiler = new DecacCompiler(null, null);

    @Test
    public void testNoOperation() throws ContextualError {
        // Same with emptyMain, nothing to verify => test to ensure Jacoco coverage
        NoOperation nothing = new NoOperation();
        assertDoesNotThrow(() -> 
            nothing.verifyInst(compiler, null, null, compiler.environmentType.VOID)
        );
    }
}
