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

public class TestSelection {

    DecacCompiler compiler;
    ClassDefinition aDef;
    ClassDefinition bDef;

    @Mock AbstractExpr object;
    @Mock AbstractIdentifier className;
    @Mock AbstractIdentifier superClassName;
    @Mock AbstractIdentifier objectClassName;
    @Mock AbstractIdentifier fieldName;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        // class A (extends Object)
        Symbol a = compiler.createSymbol("A");
        when(className.getName()).thenReturn(a);
        
        when(objectClassName.getName()).thenReturn(compiler.createSymbol("Object"));
        when(objectClassName.verifyType(compiler)).thenReturn(compiler.environmentType.OBJECT);

        DeclClass classA = new DeclClass(className, objectClassName, new ListDeclField(), new ListDeclMethod());
        classA.verifyClass(compiler);
        aDef = (ClassDefinition) compiler.environmentType.defOfType(a);

        // class B extends A
        Symbol b = compiler.createSymbol("B");
        when(superClassName.getName()).thenReturn(b);
        when(className.verifyType(compiler)).thenReturn(aDef.getType());

        DeclClass classB = new DeclClass(superClassName, className, new ListDeclField(), new ListDeclMethod());
        classB.verifyClass(compiler);
        bDef = (ClassDefinition) compiler.environmentType.defOfType(b);
    }

    @Test
    public void testPublicSelection() throws ContextualError {
        Type bType = bDef.getType();
        when(object.verifyExpr(compiler, null, null)).thenReturn(bType);

        // public field x in A
        Type INT = compiler.environmentType.INT;
        FieldDefinition xDef = new FieldDefinition(INT, Location.BUILTIN, Visibility.PUBLIC, aDef, 1);
        
        EnvironmentExp envExpB = bDef.getMembers();
        when(fieldName.verifyFieldIdent(compiler, envExpB)).thenReturn(xDef);
        
        // select x from an instance of B (subclass of A)
        Selection selection = new Selection(object, fieldName);
        assertEquals(INT, selection.verifyExpr(compiler, null, null));

        verify(fieldName).setDefinition(xDef);
    }

    @Test
    public void testProtectedValidSelection() throws ContextualError {
        Type bType = bDef.getType();
        EnvironmentExp envExpB = bDef.getMembers();
        when(object.verifyExpr(compiler, envExpB, bDef)).thenReturn(bType);

        // protected field x in A, visible in B because selected from an instance of B
        FieldDefinition xDef = new FieldDefinition(compiler.environmentType.INT, Location.BUILTIN, Visibility.PROTECTED, aDef, 1);
        when(fieldName.verifyFieldIdent(compiler, envExpB)).thenReturn(xDef);

        Selection selection = new Selection(object, fieldName);
        selection.verifyExpr(compiler, envExpB, bDef);
    }


    @Test
    public void testProtectedInvalidSelection() throws ContextualError {
        Type aType = aDef.getType();
        EnvironmentExp envExpB = bDef.getMembers();
        when(object.verifyExpr(compiler, envExpB, bDef)).thenReturn(aType);

        // protected field x in A, not visible in a method of B that selects it from an instance of A
        FieldDefinition xDef = new FieldDefinition(compiler.environmentType.INT, Location.BUILTIN, Visibility.PROTECTED, aDef, 1);
        when(fieldName.verifyFieldIdent(compiler, aDef.getMembers())).thenReturn(xDef);

        Selection selection = new Selection(object, fieldName);
        
        assertThrows(
            ContextualError.class, 
            () -> { selection.verifyExpr(compiler, envExpB, bDef); }, 
            "Excepted Contextual Error when trying to access a protected field of A from unrelated class B");
    }
}