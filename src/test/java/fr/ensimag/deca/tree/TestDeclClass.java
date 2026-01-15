package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.TypeDefinition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestDeclClass {
    DecacCompiler compiler;
    AbstractDeclClass A;
    AbstractDeclClass B;
    Symbol classSymbol;
    Symbol superSymbol;

    @Mock AbstractIdentifier className;
    @Mock AbstractIdentifier classNameCopy;
    @Mock AbstractIdentifier superClassName;
    @Mock AbstractIdentifier objectClassName;
    @Mock ListDeclField fields;
    @Mock ListDeclMethod methods;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        classSymbol = compiler.createSymbol("A");
        superSymbol = compiler.createSymbol("B");

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
            "Expected ContextualError for a class extending a non-class type during pass 1."
        );
    }

    // Pass 2 of [SyntaxeContextuelle]. Verify call verification of members of class declarations.
    @Test
    public void testFieldAndMethodVerify() throws ContextualError {
        when(objectClassName.verifyType(compiler)).thenReturn(compiler.environmentType.OBJECT);

        A = new DeclClass(className, objectClassName, fields, methods);
        A.verifyClass(compiler); // execute pass 1 check to declare class A
        
        ClassDefinition aDef = (ClassDefinition) compiler.environmentType.defOfType(className.getName());

        A.verifyClassMembers(compiler); // execute pass 2 verification
        
        verify(fields).verifyListFieldMembers(compiler, aDef);
        verify(methods).verifyListMethodMembers(compiler, aDef);

    }

    // Pass 3 of [SyntaxeContextuelle]. Verify body of class declarations.
    @Test
    public void testValidEmptyBody() throws ContextualError {
        when(objectClassName.verifyType(compiler)).thenReturn(compiler.environmentType.OBJECT);

        A = new DeclClass(className, objectClassName, fields, methods);
        A.verifyClass(compiler); // declare class A

        A.verifyClassMembers(compiler); // populate environment with members

        A.verifyClassBody(compiler); // should not raise any error

        ClassDefinition aDef = (ClassDefinition) compiler.environmentType.defOfType(className.getName());

        verify(fields).verifyListFieldRValue(compiler, aDef.getMembers(), aDef);
        verify(methods).verifyListMethodBody(compiler, aDef);
    }

}
