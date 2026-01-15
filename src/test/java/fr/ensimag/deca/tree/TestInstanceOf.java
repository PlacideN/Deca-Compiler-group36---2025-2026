package fr.ensimag.deca.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestInstanceOf {

    DecacCompiler compiler;
    ClassDefinition aDef;
    ClassDefinition bDef;

    @Mock AbstractExpr expr;
    @Mock AbstractIdentifier typeName;
    
    @Mock AbstractIdentifier classNameA;
    @Mock AbstractIdentifier classNameB;
    @Mock AbstractIdentifier objectClassName;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        // class A (extends Object)
        Symbol a = compiler.createSymbol("A");
        when(classNameA.getName()).thenReturn(a);
        when(objectClassName.getName()).thenReturn(compiler.createSymbol("Object"));
        when(objectClassName.verifyType(compiler)).thenReturn(compiler.environmentType.OBJECT);

        DeclClass declA = new DeclClass(classNameA, objectClassName, new ListDeclField(), new ListDeclMethod());
        declA.verifyClass(compiler); 
        aDef = (ClassDefinition) compiler.environmentType.defOfType(a);

        // class B (extends A)
        Symbol b = compiler.createSymbol("B");
        when(classNameB.getName()).thenReturn(b);
        when(classNameA.verifyType(compiler)).thenReturn(aDef.getType());

        DeclClass declB = new DeclClass(classNameB, classNameA, new ListDeclField(), new ListDeclMethod());
        declB.verifyClass(compiler);
        bDef = (ClassDefinition) compiler.environmentType.defOfType(b);
    }

    @Test
    public void testValidInstanceOf() throws ContextualError {
        // B instanceof A
        when(expr.verifyExpr(compiler, null, null)).thenReturn(bDef.getType());
        when(typeName.verifyType(compiler)).thenReturn(aDef.getType());

        InstanceOf inst = new InstanceOf(expr, typeName);
        assertTrue(inst.verifyExpr(compiler, null, null).isBoolean());
    }

    @Test
    public void testInstanceOfInvalidNonClass() throws ContextualError {
        // int instanceof A
        when(expr.verifyExpr(compiler, null, null)).thenReturn(compiler.environmentType.INT);
        when(typeName.verifyType(compiler)).thenReturn(aDef.getType());

        InstanceOf inst = new InstanceOf(expr, typeName);
        
        assertThrows(
            ContextualError.class, 
            () -> {inst.verifyExpr(compiler, null, null);},
            "Expected ContextualError when using instanceof with non-class type"
        );
    }
}