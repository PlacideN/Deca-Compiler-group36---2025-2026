package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestDeclMethod {
    @Mock AbstractIdentifier returnType;
    @Mock ListDeclParam params;
    @Mock ClassDefinition classDef;
    @Mock ClassDefinition superClassDef;
    @Mock EnvironmentExp env;
    @Mock EnvironmentExp superEnv;
    @Mock AbstractMethodBody methodBody;
    @Mock ClassType classType;
    
    DecacCompiler compiler;
    Symbol name;
    Symbol equals;
    AbstractIdentifier method;
    AbstractIdentifier firstMethod;
    AbstractIdentifier secondMethod;
    AbstractIdentifier equalsMethod;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);
        name = compiler.createSymbol("method");
        equals = compiler.createSymbol("equals");

        method = new Identifier(name);
        equalsMethod = new Identifier(equals);
        firstMethod = new Identifier(compiler.createSymbol("m1"));
        secondMethod = new Identifier(compiler.createSymbol("m2"));

        when(classDef.getMembers()).thenReturn(env);
        when(classDef.getSuperClass()).thenReturn(superClassDef);
        when(superClassDef.getMembers()).thenReturn(superEnv);

        Symbol classSymbol = compiler.symbolTable.create("A");
        when(classDef.getType()).thenReturn(classType);
        when(classType.getName()).thenReturn(classSymbol);
    }

    @Test
    public void testDoubleMethodDeclaration() throws ContextualError, DoubleDefException {
        Type INT = compiler.environmentType.INT;
        when(returnType.verifyType(compiler)).thenReturn(INT);
        when(params.verifyListParamType(compiler)).thenReturn(new Signature());
        
        doThrow(new DoubleDefException()).when(env).declare(eq(name), any(MethodDefinition.class));

        DeclMethod newMethod = new DeclMethod(returnType, method, params, methodBody);

        assertThrows(
            ContextualError.class, 
            () -> { newMethod.verifyMethodMembers(compiler, classDef); }, 
            "Should fail when two methods have the same name in the same class");
    }

    @Test
    public void testVoidParam() throws ContextualError {
        when(params.verifyListParamType(compiler)).thenThrow(new ContextualError("Params cannot be void", null));
        DeclMethod declMethod = new DeclMethod(returnType, method, params, methodBody);

        assertThrows(
            ContextualError.class,
            () -> declMethod.verifyMethodMembers(compiler, classDef),
            "ContextualError should be thrown when a method has a parameter of type void");
    }

    @Test
    public void testValidOverride() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        when(returnType.verifyType(compiler)).thenReturn(INT);

        Signature superMethodSignature = new Signature();
        superMethodSignature.add(INT);
        when(params.verifyListParamType(compiler)).thenReturn(superMethodSignature);

        MethodDefinition superMethod = new MethodDefinition(INT, Location.BUILTIN, superMethodSignature, 3);
        when(superEnv.get(name)).thenReturn(superMethod);

        DeclMethod newMethod = new DeclMethod(returnType, method, params, methodBody);
        newMethod.verifyMethodMembers(compiler, classDef);

        assertEquals(3, method.getMethodDefinition().getIndex());
    }

    @Test
    public void testSignatureMismatch() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        Type FLOAT = compiler.environmentType.FLOAT;
        
        when(returnType.verifyType(compiler)).thenReturn(INT);

        // mock a child method with one int param
        Signature sig = new Signature();
        sig.add(INT);
        when(params.verifyListParamType(compiler)).thenReturn(sig);

        // mock a parent method with a float type
        Signature superSig = new Signature();
        superSig.add(FLOAT);
        MethodDefinition superMethod = new MethodDefinition(INT, Location.BUILTIN, superSig, 1);
        when(superEnv.get(name)).thenReturn(superMethod);

        DeclMethod decl = new DeclMethod(returnType, method, params, methodBody);

        assertThrows(
            ContextualError.class, 
            () -> decl.verifyMethodMembers(compiler, classDef),
            "ContextualError should be thrown when declaring a method with a signature that does not match the overridden method");
    }

    @Test
    public void testIndexIncrement() throws ContextualError {
        Type VOID = compiler.environmentType.VOID;
        when(returnType.verifyType(compiler)).thenReturn(VOID);
        when(params.verifyListParamType(compiler)).thenReturn(new Signature());

        // use any() matcher instead to work with both "m1" and "m2" names instead of 
        // initializing name for each method during setup
        when(superEnv.get(any(Symbol.class))).thenReturn(null);

        // 0 is equals, so first custom method gets index 1, next 2
        when(classDef.incNumberOfMethods()).thenReturn(1, 2); 

        DeclMethod firstDeclMethod = new DeclMethod(returnType, firstMethod, params, methodBody);
        firstDeclMethod.verifyMethodMembers(compiler, classDef);

        assertEquals(1, firstMethod.getMethodDefinition().getIndex(), "The first method should have index 1");

        DeclMethod secondDeclMethod = new DeclMethod(returnType, secondMethod, params, methodBody);
        secondDeclMethod.verifyMethodMembers(compiler, classDef);
        assertEquals(2, secondMethod.getMethodDefinition().getIndex(), "The second method should have incremented the index to 2");
    }

    @Test
    public void testIsReturnTypeValidSubtype() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        when(returnType.verifyType(compiler)).thenReturn(INT);

        Signature sig = new Signature();
        when(params.verifyListParamType(compiler)).thenReturn(new Signature());

        MethodDefinition superMethod = new MethodDefinition(INT, Location.BUILTIN, sig, 2);
        when(superEnv.get(name)).thenReturn(superMethod);

        DeclMethod newMethod = new DeclMethod(returnType, method, params, methodBody);
        newMethod.verifyMethodMembers(compiler, classDef);

        assertEquals(2, method.getMethodDefinition().getIndex());
    }

    @Test
    public void testReturnTypeIsNotSubtype() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        Type FLOAT = compiler.environmentType.FLOAT;

        // child method returns FLOAT
        when(returnType.verifyType(compiler)).thenReturn(FLOAT);
        when(params.verifyListParamType(compiler)).thenReturn(new Signature());

        // super method returns INT
        MethodDefinition superMethod = new MethodDefinition(INT, Location.BUILTIN, new Signature(), 1);
        when(superEnv.get(name)).thenReturn(superMethod);

        DeclMethod newMethod = new DeclMethod(returnType, method, params, methodBody);

        assertThrows(
            ContextualError.class, 
            () -> { newMethod.verifyMethodMembers(compiler, classDef); },
            "ContextualError should be thrown when the child method return type is not a subtype of parent return type");
    }

    @Test
    public void testMethodCollisionWithSuperField() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        when(returnType.verifyType(compiler)).thenReturn(INT);
        when(params.verifyListParamType(compiler)).thenReturn(new Signature());

        FieldDefinition superField = new FieldDefinition(INT, Location.BUILTIN, Visibility.PUBLIC, superClassDef, 1);
        when(superEnv.get(name)).thenReturn(superField);

        DeclMethod newMethod = new DeclMethod(returnType, method, params, methodBody);

        assertThrows(
            ContextualError.class, 
            () -> { newMethod.verifyMethodMembers(compiler, classDef); }, 
            "ContextualError should be thrown when a method name hides a field from the superclass");
    }

    @Test
    public void testValidEqualsOverride() throws ContextualError {
        Type BOOLEAN = compiler.environmentType.BOOLEAN;
        Type OBJECT = compiler.environmentType.OBJECT;
        
        when(returnType.verifyType(compiler)).thenReturn(BOOLEAN);
        Signature sig = new Signature();
        sig.add(OBJECT);
        when(params.verifyListParamType(compiler)).thenReturn(sig);

        MethodDefinition superMethod = new MethodDefinition(BOOLEAN, Location.BUILTIN, sig, 0);
        when(superEnv.get(equals)).thenReturn(superMethod);

        DeclMethod method = new DeclMethod(returnType, equalsMethod, params, methodBody);
        method.verifyMethodMembers(compiler, classDef);
        assertEquals(0, equalsMethod.getMethodDefinition().getIndex());
    }

    // Pass 3 of [SyntaxeContextuelle]. Verify the body of method declarations.
    @Test
    public void testMethodBodyVerify() throws ContextualError {
        // mock a method which returns an int type
        Type INT = compiler.environmentType.INT;
        MethodDefinition def = new MethodDefinition(INT, Location.BUILTIN, new Signature(), 1);
        method.setDefinition(def);

        EnvironmentExp classEnv = new EnvironmentExp(null);
        EnvironmentExp paramEnv = new EnvironmentExp(null);
        when(classDef.getMembers()).thenReturn(classEnv);
        when(params.verifyListParam(compiler, classEnv)).thenReturn(paramEnv);

        DeclMethod decl = new DeclMethod(returnType, method, params, methodBody);
        decl.verifyMethodBody(compiler, classDef);

        verify(methodBody).verifyMethodBody(compiler, paramEnv, classDef, INT);
    }

}