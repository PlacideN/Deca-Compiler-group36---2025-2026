package fr.ensimag.deca.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestNew {
    private DecacCompiler compiler;
    private ClassDefinition aDef;

    @Mock AbstractIdentifier className;
    @Mock AbstractIdentifier objectClassName;

    @BeforeEach
    void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        // class A (extends Object)
        Symbol a = compiler.createSymbol("A");
        when(className.getName()).thenReturn(a);
        when(objectClassName.getName()).thenReturn(compiler.createSymbol("Object"));
        when(objectClassName.verifyType(compiler)).thenReturn(compiler.environmentType.OBJECT);

        DeclClass classDecl = new DeclClass(className, objectClassName, new ListDeclField(), new ListDeclMethod());
        classDecl.verifyClass(compiler);
        aDef = (ClassDefinition) compiler.environmentType.defOfType(a);
    }

    @Test
    public void testValidClassInstance() throws ContextualError {
        when(className.verifyType(compiler)).thenReturn(aDef.getType());

        New object = new New(className);

        assertEquals(aDef.getType(), object.verifyExpr(compiler, null, null));
    }

    @Test
    public void testInstanceOfInvalidType() throws ContextualError {
        when(className.verifyType(compiler)).thenReturn(compiler.environmentType.INT);
        New object = new New(className);
        assertThrows(
            ContextualError.class, 
            () -> {object.verifyExpr(compiler, null, null);},
            "Expected ContextualError when instantiating an non-class type (int)"
        );
    }
}