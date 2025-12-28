package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
    

public class TestIfThenElse {
    DecacCompiler compiler;
    EnvironmentExp env;

    @BeforeEach
    public void setup() {
        compiler = new DecacCompiler(null, null);
        env = new EnvironmentExp(null);
    }
    
    @Test
    public void testCorrectIfCondition() throws ContextualError {
        // if (true) { } else { }
        AbstractExpr cond = new BooleanLiteral(true);
        ListInst thenBranch = new ListInst();
        ListInst elseBranch = new ListInst();
        
        IfThenElse loop = new IfThenElse(cond, thenBranch, elseBranch);
        loop.verifyInst(compiler, env, null, compiler.environmentType.VOID);
    }

    @Test
    public void testIncorrectIfCondition() {
        // if (1) { }
        AbstractExpr cond = new IntLiteral(1);
        IfThenElse loop = new IfThenElse(cond, new ListInst(), new ListInst());

        assertThrows(
            ContextualError.class, 
            () -> loop.verifyInst(compiler, env, null, compiler.environmentType.VOID),
            "Should throw ContextualError because 1 is not a valid condition for an if statement (expected boolean)."
        );
    }


}