package fr.ensimag.deca.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestDeclParam {

    @Mock AbstractIdentifier type;
    @Mock AbstractIdentifier paramName;
    @Mock EnvironmentExp env;

    DecacCompiler compiler;
    Symbol name;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);
        
        name = compiler.symbolTable.create("x");
        when(paramName.getName()).thenReturn(name);
    }

    // Pass 2 of [Syntaxe Contextuelle]
    @Test
    public void testInvalidVoidParam() throws ContextualError {
        when(type.verifyType(compiler)).thenReturn(compiler.environmentType.VOID);
        DeclParam declParam = new DeclParam(type, paramName);

        assertThrows(
            ContextualError.class, 
            () -> { declParam.verifyParamType(compiler); }, 
            "Expected ContextualError for void parameter type");
    }

    @Test
    public void testValidParam() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        when(type.verifyType(compiler)).thenReturn(INT);
        DeclParam declParam = new DeclParam(type, paramName);

        assertEquals(INT, declParam.verifyParamType(compiler));
    }

    // Pass 3 of [Syntaxe Contextuelle]
    @Test
    public void testVerifyParamDecoration() throws ContextualError, DoubleDefException {
        Type FLOAT = compiler.environmentType.FLOAT;
        when(type.verifyType(compiler)).thenReturn(FLOAT);

        DeclParam declParam = new DeclParam(type, paramName);
        declParam.verifyParamName(compiler, env, -3);

        // definition is created inside the declare method => use matchers
        verify(env).declare(eq(name), any(ParamDefinition.class));
        verify(paramName).setDefinition(any(ParamDefinition.class));
    }

    @Test
    public void testDoubleParamDefinition() throws ContextualError, DoubleDefException {
        Type INT = compiler.environmentType.INT;
        when(type.verifyType(compiler)).thenReturn(INT);

        // use doThrow because declare is a void method
        doThrow(new DoubleDefException()).when(env).declare(eq(name), any(ParamDefinition.class));

        DeclParam declParam = new DeclParam(type, paramName);
        
        assertThrows(
            ContextualError.class,
            () -> { declParam.verifyParamName(compiler, env, -3); },
            "Expected ContextualError after double definition of parameter in the same method");
    }
}