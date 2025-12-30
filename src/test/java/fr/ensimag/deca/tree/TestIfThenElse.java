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
        
        IfThenElse block = new IfThenElse(cond, thenBranch, elseBranch);
        block.verifyInst(compiler, env, null, compiler.environmentType.VOID);
    }

    @Test
    public void testIfWithComparisonCondition() throws ContextualError {
        // if (1 < 2) { }
        AbstractExpr left = new IntLiteral(1);
        AbstractExpr right = new IntLiteral(2);
        
        AbstractExpr cond = new Lower(left, right);
        
        IfThenElse block = new IfThenElse(cond, new ListInst(), new ListInst());
        block.verifyInst(compiler, env, null, compiler.environmentType.VOID);
    }

    @Test
    public void testIncorrectIfCondition() {
        // if (1) { }
        AbstractExpr cond = new IntLiteral(1);
        IfThenElse block = new IfThenElse(cond, new ListInst(), new ListInst());

        assertThrows(
            ContextualError.class, 
            () -> block.verifyInst(compiler, env, null, compiler.environmentType.VOID),
            "Should throw ContextualError because 1 is not a valid condition for an if statement (expected boolean)."
        );
    }

}