package fr.ensimag.deca.tree;

import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.SymbolTable.Symbol;


public class TestListDeclVar {
    @Mock
    EnvironmentExp localEnv;

    DecacCompiler compiler;
    ListDeclVar listDeclVar;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);
        listDeclVar = new ListDeclVar();
    }

    @Test
    public void testEmptyListDeclVar() throws ContextualError {
        // Should not crash and return an environement identical to the input
        assertDoesNotThrow(() -> {
        listDeclVar.verifyListDeclVariable(compiler, localEnv, null);},
        "Should not throw an exception for an empty ListDeclVar !");

        verifyNoInteractions(localEnv);
    }

    @Test
    public void testSequentialListDeclVar() throws ContextualError {
        EnvironmentExp env = new EnvironmentExp(null);

        Identifier intType = new Identifier(compiler.createSymbol("int"));
        intType.verifyType(compiler);

        // int x;
        Symbol sx = compiler.createSymbol("x");
        Identifier xVar = new Identifier(sx);
        DeclVar xDecl = new DeclVar(intType, xVar, new NoInitialization());
        
        // int y = x;
        Symbol sy = compiler.createSymbol("y");
        Identifier yVar = new Identifier(sy);
        Identifier yInitVar = new Identifier(sx);
        Initialization yInit = new Initialization(yInitVar);
        DeclVar yDecl = new DeclVar(intType, yVar, yInit);

        listDeclVar.add(xDecl);
        listDeclVar.add(yDecl);

        listDeclVar.verifyListDeclVariable(compiler, env, null);

        // 'x' used inside 'int y = x' initialization should point to the same 'x'
        assertEquals(env.get(sx), yInitVar.getDefinition());

        // 'y' should be declared in the environment
        assertNotNull(env.get(sy));
    }

    @Test
    public void testUndefinedSequentialListDeclVar() throws ContextualError {
        EnvironmentExp env = new EnvironmentExp(null);
        Identifier intType = new Identifier(compiler.createSymbol("int"));
        intType.verifyType(compiler);

        // int y = z;
        Symbol sy = compiler.createSymbol("y");
        Symbol sz = compiler.createSymbol("z"); // 'z' is not declared
        
        Initialization yInit = new Initialization(new Identifier(sz));
        DeclVar yDecl = new DeclVar(intType, new Identifier(sy), yInit);
        listDeclVar.add(yDecl);

        assertThrows(
            ContextualError.class, 
            () -> listDeclVar.verifyListDeclVariable(compiler, env, null),
            "Should throw a ContextualError when a variable is used before being declared!"
        );
    }

}