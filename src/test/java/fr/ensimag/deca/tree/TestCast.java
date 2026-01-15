package fr.ensimag.deca.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestCast {

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

        // class B extends A
        Symbol b = compiler.createSymbol("B");
        when(classNameB.getName()).thenReturn(b);
        when(classNameA.verifyType(compiler)).thenReturn(aDef.getType());

        DeclClass declB = new DeclClass(classNameB, classNameA, new ListDeclField(), new ListDeclMethod());
        declB.verifyClass(compiler);
        bDef = (ClassDefinition) compiler.environmentType.defOfType(b);
    }

    @Test
    public void testEqualsCast() throws ContextualError {
        // Object o; A a = (A) o;
        Type objectType = compiler.environmentType.OBJECT;
        when(expr.verifyExpr(compiler, null, null)).thenReturn(objectType);
        
        Type type = aDef.getType();
        when(typeName.verifyType(compiler)).thenReturn(type);

        Cast cast = new Cast(typeName, expr);
        assertEquals(type, cast.verifyExpr(compiler, null, null));
    }

    @Test
    public void testValidDowncast() throws ContextualError {
        // A a; B b = (B) a; Valid downcast during compilation
        when(expr.verifyExpr(compiler, null, null)).thenReturn(aDef.getType());
        when(typeName.verifyType(compiler)).thenReturn(bDef.getType());

        Cast cast = new Cast(typeName, expr);
        assertEquals(bDef.getType(),  cast.verifyExpr(compiler, null, null));
    }

    @Test
    public void testValidUpcast() throws ContextualError {
        // b = new B(); A a = (A) b; Valid upcast
        when(expr.verifyExpr(compiler, null, null)).thenReturn(bDef.getType());
        when(typeName.verifyType(compiler)).thenReturn(aDef.getType());

        Cast cast = new Cast(typeName, expr);
        assertEquals(aDef.getType(), cast.verifyExpr(compiler, null, null));
    }

    @Test
    public void testCastInvalidIncompatible() throws ContextualError {
        // int x = (A) int;
        when(expr.verifyExpr(compiler, null, null)).thenReturn(compiler.environmentType.INT);
        when(typeName.verifyType(compiler)).thenReturn(aDef.getType());

        Cast cast = new Cast(typeName, expr);
        
        assertThrows(
            ContextualError.class, 
            () -> { cast.verifyExpr(compiler, null, null);},
            "Expected ContextualError when casting between incompatible types (int to A)");
    }
}