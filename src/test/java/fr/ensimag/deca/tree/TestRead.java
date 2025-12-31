package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestRead {

    private DecacCompiler compiler;
    private EnvironmentExp env;

    @BeforeEach
    public void setup() {
        compiler = new DecacCompiler(null, null);
        env = new EnvironmentExp(null);
    }

    @Test
    public void testReadIntType() throws ContextualError {
        ReadInt read = new ReadInt();
        
        Type type = read.verifyExpr(compiler, env, null);

        // Check if the type exists in the compiler's type environment
        assertEquals(compiler.environmentType.INT, type);
    }

    @Test
    public void testReadFloatType() throws ContextualError {
        ReadFloat read = new ReadFloat();
        
        Type type = read.verifyExpr(compiler, env, null);
             
        assertEquals(compiler.environmentType.FLOAT, type); 
    }
}