package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestAssign {
    DecacCompiler compiler;
    EnvironmentExp env;
    Identifier intType;

    // int x;
    Symbol x;
    Identifier xVar;
    DeclVar xDecl;

    @BeforeEach
    public void setup() throws ContextualError {
        compiler = new DecacCompiler(null, null);
        env = new EnvironmentExp(null);

        intType = new Identifier(compiler.createSymbol("int"));
        intType.verifyType(compiler);

        // int x;
        x = compiler.createSymbol("x");
        xVar = new Identifier(x);
        xDecl = new DeclVar(intType, xVar, new NoInitialization());
    }

    @Test
    public void testSuccessfulAssign() throws ContextualError {
        xDecl.verifyDeclVar(compiler, env, null, 1, Register.GB); // Declare x in env

        Assign assign = new Assign(xVar, new IntLiteral(5)); // x = 5;
        assign.verifyExpr(compiler, env, null);
    }

    @Test
    public void testIncompatibleAssign() throws ContextualError {
        xDecl.verifyDeclVar(compiler, env, null, 1, Register.GB); // Declare x in env

        Assign assign = new Assign(xVar, new BooleanLiteral(true)); // x = true;
        assertThrows(
            ContextualError.class,
            () -> assign.verifyExpr(compiler, env, null),
            "Should throw ContextualError for incompatible type (int, boolean) in assignment"
        );
    }

    @Test
    public void testUndeclaredAssign() {
        Identifier yVar = new Identifier(compiler.createSymbol("y")); // y is not declared in env
        Assign assign = new Assign(yVar, new IntLiteral(5)); // y = 5;

        assertThrows(
            ContextualError.class,
            () -> assign.verifyExpr(compiler, env, null),
            "Should throw ContextualError for assignment to undeclared variable"
        );
    }
    @Test
    public void testIntToFloatAssign() throws ContextualError {
        // float f;
        Identifier floatType = new Identifier(compiler.createSymbol("float"));
        floatType.verifyType(compiler);

        Identifier fVar = new Identifier(compiler.createSymbol("f"));
        DeclVar fDecl = new DeclVar(floatType, fVar, new NoInitialization());
        fDecl.verifyDeclVar(compiler, env, null, 1, Register.GB); // Declare f in env

        Assign assign = new Assign(fVar, new IntLiteral(10));
        assign.verifyExpr(compiler, env, null);

        assertTrue(assign.getRightOperand() instanceof ConvFloat);
        assertEquals(compiler.environmentType.FLOAT, assign.getRightOperand().getType());
    }
}
