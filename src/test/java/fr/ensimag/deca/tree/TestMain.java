package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.DecacCompiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;


public class TestMain {

    @Mock
    ListDeclVar listDeclVar;
    @Mock
    ListInst listInst;
    
    DecacCompiler compiler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);
    }

    @Test
    public void testMainWithInstructions() throws ContextualError {
        Main main = new Main(listDeclVar, listInst);

        // Main creates EnvironmentExp internally => we intercept it with a captor
        ArgumentCaptor<EnvironmentExp> captor = ArgumentCaptor.forClass(EnvironmentExp.class);

        main.verifyMain(compiler);

        verify(listDeclVar).verifyListDeclVariable(eq(compiler), captor.capture(), isNull());

        EnvironmentExp capturedEnvExp = captor.getValue();
        assertNotNull(capturedEnvExp, "EnvironmentExp should not be null");

        verify(listInst).verifyListInst(eq(compiler), eq(capturedEnvExp), isNull(), eq(compiler.environmentType.VOID));

    }

    @Test
    public void testEmptyMain() {
        EmptyMain empty = new EmptyMain();
        assertDoesNotThrow(() -> empty.verifyMain(compiler),
            "Should not raise Contextual Error when main bloc is empty"
        );
    }
}
