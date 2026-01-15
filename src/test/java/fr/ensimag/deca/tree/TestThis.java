package fr.ensimag.deca.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestThis {
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
    public void testValidThis() throws ContextualError {
        This object = new This();
        Type result = object.verifyExpr(compiler, null, aDef);

        assertEquals(aDef.getType(), result);
    }

    @Test
    public void testInvalidThisInMain() {
        This object = new This();
        assertThrows(
            ContextualError.class, 
            () -> { object.verifyExpr(compiler, null, null); },
            "Expected ContextualError when using 'this' inside of Main"
        );
    }
    
}
