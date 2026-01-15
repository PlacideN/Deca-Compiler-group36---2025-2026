package fr.ensimag.deca.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class TestDeclField {

    @Mock AbstractIdentifier fieldType;
    @Mock AbstractIdentifier fieldName;
    @Mock ClassDefinition classDef;
    @Mock ClassDefinition superClassDef;
    @Mock EnvironmentExp superEnv;
    @Mock EnvironmentExp env;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);

        Symbol x = compiler.symbolTable.create("x");
        when(fieldName.getName()).thenReturn(x);

        when(classDef.getMembers()).thenReturn(env);
        when(classDef.getSuperClass()).thenReturn(superClassDef);
        when(superClassDef.getMembers()).thenReturn(superEnv);
    }

    // Pass 2 of [Syntaxe Contextuelle]
    @Test
    public void testVoidFieldDeclaration() throws ContextualError {
        when(fieldType.verifyType(compiler)).thenReturn(compiler.environmentType.VOID);
        DeclField declField = new DeclField(Visibility.PUBLIC, fieldType, fieldName, new NoInitialization());

        assertThrows(
            ContextualError.class,
            () -> declField.verifyFieldMembers(compiler, classDef),
            "Expected ContextualError when declaring a field of type void"
        );
    }

    @Test
    public void testDoubleFieldDeclaration() throws DoubleDefException, ContextualError {
        when(fieldType.verifyType(compiler)).thenReturn(compiler.environmentType.INT);
        
        Symbol symbol = fieldName.getName();
        doThrow(new DoubleDefException()).when(env).declare(eq(symbol), any(FieldDefinition.class));

        DeclField declField = new DeclField(Visibility.PUBLIC, fieldType, fieldName, new NoInitialization());

        assertThrows(
            ContextualError.class,
            () -> declField.verifyFieldMembers(compiler, classDef),
            "Expected ContextualError when declaring a field with a name that already exists in the class"
        );
    }

    @Test
    public void testFieldHiding() throws ContextualError {
        Type INT = compiler.environmentType.INT;
        when(fieldType.verifyType(compiler)).thenReturn(INT);
        
        MethodDefinition methodDef = new MethodDefinition(INT, Location.BUILTIN, new Signature(), 0);
        when(superEnv.get(fieldName.getName())).thenReturn(methodDef);

        DeclField declField = new DeclField(Visibility.PUBLIC, fieldType, fieldName, new NoInitialization());

        assertThrows(
            ContextualError.class,
            () -> declField.verifyFieldMembers(compiler, classDef),
            "Expected ContextualError when declaring a field that hides a method from the superclass"
        );
    }

    // Pass 3 of [Syntaxe Contextuelle]
    @Test
    public void testInitFieldCompatibility() throws ContextualError {
        // int x = false;
        Type INT = compiler.environmentType.INT;
        when(fieldType.verifyType(compiler)).thenReturn(INT);

        FieldDefinition xDef = new FieldDefinition(INT, Location.BUILTIN, Visibility.PUBLIC, classDef, 1);
        when(fieldName.getFieldDefinition()).thenReturn(xDef);

        DeclField declField = new DeclField(Visibility.PUBLIC, fieldType, fieldName, new Initialization(new BooleanLiteral(false)));

        assertThrows(
            ContextualError.class,
            () -> declField.verifyFieldRValue(compiler, classDef),
            "Expected ContextualError when initializing a field with an incompatible type"
        );
    }

    @Test
    public void testInitWithOtherField() throws ContextualError, DoubleDefException {
        // int x = y;
        Type INT = compiler.environmentType.INT;
        when(fieldType.verifyType(compiler)).thenReturn(INT);

        Symbol y = compiler.symbolTable.create("y");
        FieldDefinition yDef = new FieldDefinition(INT, Location.BUILTIN, Visibility.PUBLIC, classDef, 1);
        env.declare(y, yDef);

        FieldDefinition xDef = new FieldDefinition(INT, Location.BUILTIN, Visibility.PUBLIC, classDef, 2);
        when(fieldName.getFieldDefinition()).thenReturn(xDef);

        AbstractIdentifier yName = mock(AbstractIdentifier.class);
        when(yName.verifyExpr(compiler, env, classDef)).thenReturn(INT);
        DeclField declField = new DeclField(Visibility.PUBLIC, fieldType, fieldName, new Initialization(yName));
        declField.verifyFieldRValue(compiler, classDef);        
    }
    
}
