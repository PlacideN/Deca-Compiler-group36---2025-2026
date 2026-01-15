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
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestMethodCall {

    @Mock AbstractExpr object;
    @Mock AbstractIdentifier className;
    @Mock AbstractIdentifier objectClassName;
    @Mock AbstractIdentifier methodName;
    @Mock ListExpr params;
    
    DecacCompiler compiler;
    ClassType aClassType;
    ClassDefinition aDef;
    Signature sig;

    @BeforeEach
    void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        // class A (extends Object)
        Symbol aSymbol = compiler.createSymbol("A");
        when(className.getName()).thenReturn(aSymbol);
        
        when(objectClassName.getName()).thenReturn(compiler.createSymbol("Object"));
        when(objectClassName.verifyType(compiler)).thenReturn(compiler.environmentType.OBJECT);

        DeclClass declA = new DeclClass(className, objectClassName, new ListDeclField(), new ListDeclMethod());
        declA.verifyClass(compiler);  // declare class A
        aDef = (ClassDefinition) compiler.environmentType.defOfType(aSymbol);


        // define a method that takes a float and returns an int
        sig = new Signature();
        sig.add(compiler.environmentType.FLOAT);
        MethodDefinition methodDef = new MethodDefinition(compiler.environmentType.INT, Location.BUILTIN, sig, 1);
        
        when(methodName.verifyMethodIdent(compiler, aDef.getMembers())).thenReturn(methodDef);
    }

    @Test
    public void testValidMethodCall() throws ContextualError {
        when(object.verifyExpr(compiler, null, aDef)).thenReturn(aDef.getType());

        MethodCall call = new MethodCall(object, methodName, params);

        assertEquals(compiler.environmentType.INT, call.verifyExpr(compiler, null, aDef));
        verify(params).verifyListExprSignature(compiler, null, aDef, sig);
    }

    @Test
    public void testWrongArgsInCall() throws ContextualError {
        when(object.verifyExpr(compiler, null, aDef)).thenReturn(aDef.getType());

        ListExpr callParams = new ListExpr();
        callParams.add(mock(AbstractExpr.class));
        callParams.add(mock(AbstractExpr.class));

        MethodCall call = new MethodCall(object, methodName, callParams);

        assertThrows(
            ContextualError.class, 
            () -> { call.verifyExpr(compiler, null, aDef); }, 
            "Expected ContextualError due to wrong number of arguments");
    }

    @Test
    public void testInvalidTypeCall() throws ContextualError {
        when(object.verifyExpr(compiler, null, null)).thenReturn(compiler.environmentType.INT);

        MethodCall call = new MethodCall(object, methodName, params);
        assertThrows(
            ContextualError.class, 
            () -> call.verifyExpr(compiler, null, null),
            "Expected ContextualError due to invalid object type"
        );
    }
}