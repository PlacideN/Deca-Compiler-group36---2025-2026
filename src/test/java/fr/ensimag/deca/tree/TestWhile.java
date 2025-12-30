package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
    

public class TestWhile {
    
    DecacCompiler compiler;
    EnvironmentExp env;

    @BeforeEach
    public void setup() {
        compiler = new DecacCompiler(null, null);
        env = new EnvironmentExp(null);
    }
    
    @Test
    public void testCorrectWhileCondition() throws ContextualError {
        // while (true) { }
        AbstractExpr cond = new BooleanLiteral(true);
        ListInst body = new ListInst();
        
        While loop = new While(cond, body);
        loop.verifyInst(compiler, env, null, compiler.environmentType.VOID);
    }

    @Test
    public void testIncorrectWhileCondition() {
        // while (1) { }
        AbstractExpr cond = new IntLiteral(1);
        While loop = new While(cond, new ListInst());

        assertThrows(
            ContextualError.class, 
            () -> loop.verifyInst(compiler, env, null, compiler.environmentType.VOID),
            "Should throw ContextualError because 1 is not a valid condition for a while statement (expected boolean)."
        );
    }

    @Test
    public void testWhileWithIncompleteBody() throws ContextualError {
        // while (true) { Inst }
        AbstractExpr cond = new BooleanLiteral(true);
        AbstractInst inst = Mockito.mock(AbstractInst.class);
        ListInst body = new ListInst();
        body.add(inst);

        While loop = new While(cond, body);
        loop.verifyInst(compiler, env, null, compiler.environmentType.VOID);

        verify(inst).verifyInst(compiler, env, null, compiler.environmentType.VOID);
    }

}