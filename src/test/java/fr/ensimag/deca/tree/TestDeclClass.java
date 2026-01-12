package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestDeclClass {
    DecacCompiler compiler;
    AbstractDeclClass A;
    AbstractDeclClass B;

    @Mock
    AbstractIdentifier className;
    @Mock
    AbstractIdentifier classNameCopy;
    @Mock
    AbstractIdentifier superClassName;
    @Mock
    AbstractIdentifier objectClassName;
    @Mock
    ListDeclField fields;
    @Mock
    ListDeclMethod methods;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        Symbol classSymbol = compiler.createSymbol("A");
        Symbol superSymbol = compiler.createSymbol("B");

        // already exists in EnvironnementExp, simply retrieve it
        Symbol objectSymbol = compiler.createSymbol("Object"); 
                
        when(className.getName()).thenReturn(classSymbol);
        when(classNameCopy.getName()).thenReturn(classSymbol);
        when(superClassName.getName()).thenReturn(superSymbol);
        when(objectClassName.getName()).thenReturn(objectSymbol); 
    }

    // Pass 1 of [SyntaxeContextuelle]. Verify the signature of class declarations.
    @Test
    public void testUndeclaredExtend() throws ContextualError {
        A = new DeclClass(className, objectClassName, fields, methods);
        A.verifyClass(compiler);

        A = new DeclClass(className, superClassName, fields, methods);
        assertThrows(ContextualError.class,
            () -> { A.verifyClass(compiler); },
            "Expected ContextualError for extending a class using an undeclared class."
        );
    }

    @Test
    public void testExtendItself() {
        // should raise an error because superclass is not yet declared
        AbstractDeclClass A = new DeclClass(className, className, fields, methods);
        assertThrows(ContextualError.class,
            () -> { A.verifyClass(compiler); },
            "Expected ContextualError for a class extending itself."
        );
    }

    @Test
    public void testDoubleDeclaration() throws ContextualError {
        A = new DeclClass(className, objectClassName, fields, methods);
        A.verifyClass(compiler); // declare class A
        B = new DeclClass(classNameCopy, objectClassName, fields, methods);
        assertThrows(ContextualError.class,
            () -> { B.verifyClass(compiler); },
            "Expected ContextualError for double declaration of class."
        );
    }

    @Test
    public void testInheritFromNonClass(){
        Type INT = compiler.environmentType.INT;
        when(superClassName.getName()).thenReturn(INT.getName());
        A = new DeclClass(className, superClassName, fields, methods);
        assertThrows(ContextualError.class,
            () -> { A.verifyClass(compiler); },
            "Expected ContextualError for a class extending a non-class type."
        );
    }
}
