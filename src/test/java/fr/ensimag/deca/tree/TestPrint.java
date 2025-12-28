package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPrint {
    DecacCompiler compiler;
    EnvironmentExp env;
    ListExpr params;

    @BeforeEach
    public void setup() {
        compiler = new DecacCompiler(null, null);
        env = new EnvironmentExp(null);
        params = new ListExpr();

    }

    @Test
    public void testCompatibleTypesPrint() throws ContextualError {
        params.add(new IntLiteral(1));
        params.add(new FloatLiteral(1.5f));
        params.add(new StringLiteral("hello"));

        AbstractPrint print = new Print(false, params); // print(1, 1.5, "hello");
        print.verifyInst(compiler, env, null, null);

        print = new Println(false, params); // println(1, 1.5, "hello");
        print.verifyInst(compiler, env, null, null);
    }

    @Test
    public void TestIncompatibleTypePrint() {
        params.add(new BooleanLiteral(true)); 

        Print print = new Print(false, params);

        assertThrows(ContextualError.class, () -> 
            print.verifyInst(compiler, env, null, null),
            "Should throw ContextualError when printing boolean"
        );
    }

    @Test
    public void testEmptyPrint() {
        Print print = new Print(true, params); // print();

        assertDoesNotThrow(() -> print.verifyInst(compiler, env, null, null));
    }
}