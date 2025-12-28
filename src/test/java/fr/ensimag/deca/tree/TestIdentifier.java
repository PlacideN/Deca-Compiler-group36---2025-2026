package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.Definition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public class TestIdentifier {

    AbstractIdentifier id;
    VariableDefinition varDef;
    TypeDefinition typeDef;
    DecacCompiler compiler;
    Symbol symbol;
    
    @Mock
    EnvironmentExp env;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        symbol = compiler.createSymbol("int"); 
        id = new Identifier(symbol);
        
        typeDef = compiler.environmentType.defOfType(symbol);
        
        varDef = new VariableDefinition(compiler.environmentType.INT, id.getLocation());
        when(env.get(symbol)).thenReturn(varDef);
    }
    
    @Test
    public void testVerifyTypeSuccess() throws ContextualError {
        Type idType = id.verifyType(compiler);
        
        assertTrue(idType.isInt());
        assertEquals(id.getDefinition(), typeDef);
    }

    @Test
    public void testLookupDefinitionInEnv() throws ContextualError {
        Type idType = id.verifyExpr(compiler, env, null);

        assertEquals(compiler.environmentType.INT, idType); // Types should match

        ExpDefinition lookupDef = id.getExpDefinition();

        // Check that the node has been decorated with the correct varDefinition
        assertEquals(varDef, lookupDef);
        verify(env).get(symbol);
    }

    @Test
    public void testVerifyTypeUndefined() {
        Identifier incorrectId = new Identifier(compiler.createSymbol("Uint")); // typo in type name (no class Uint)
        
        assertThrows(
            ContextualError.class, 
            () -> incorrectId.verifyType(compiler), 
            "Should throw ContextualError for unknown types (ie. that are not in env_types)");
    }

    @Test
    public void testUndefinedIdentifier() {
        when(env.get(symbol)).thenReturn(null); // undefined varDefinition for the symbol
        assertThrows(
            ContextualError.class,
            () -> id.verifyExpr(compiler, env, null),
            "Expected ContextualError for undefined identifier"
        );
    }
}
