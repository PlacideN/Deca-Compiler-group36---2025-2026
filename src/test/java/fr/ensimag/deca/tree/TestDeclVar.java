package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public class TestDeclVar {

    AbstractDeclVar declVar;

    @Mock
    EnvironmentExp env;
    @Mock
    AbstractIdentifier type;
    @Mock
    AbstractIdentifier varName;
    @Mock
    AbstractInitialization init;
    @Mock
    Symbol symbol;


    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        declVar = new DeclVar(type, varName, init);
        symbol = compiler.createSymbol("x");

        // Mock behaviors
        when(type.verifyType(compiler)).thenReturn(compiler.environmentType.INT);
        when(varName.getName()).thenReturn(symbol);
    }

    @Test
    public void testDeclareVar() throws ContextualError, DoubleDefException {

        declVar.verifyDeclVar(compiler, env, null);

        // Capture the VariableDefinition (name, type) passed to declare
        ArgumentCaptor<VariableDefinition> captor = ArgumentCaptor.forClass(VariableDefinition.class);

        // Check that the variable was declared in the environment with the correct name and type
        verify(env).declare(eq(symbol), captor.capture());

        // Implicit check of the retrieved type
        Definition def = captor.getValue();
        assertEquals(compiler.environmentType.INT, def.getType());
    }
    
    @Test
    public void testDoubleDeclaration() throws ContextualError, DoubleDefException {
        doThrow(new DoubleDefException()).when(env).declare(symbol, null);

        assertThrows(
            ContextualError.class, 
            () -> declVar.verifyDeclVar(compiler, env, null),
            "Expected ContextualError when caught DoubleDefException"
        );
    }

    @Test
    public void testVoidTypeDeclaration() throws ContextualError {
        when(type.verifyType(compiler)).thenReturn(compiler.environmentType.VOID);

        assertThrows(
            ContextualError.class,
            () -> declVar.verifyDeclVar(compiler, env, null),
            "Expected ContextualError when declaring a variable of void type"
        );
    }
}