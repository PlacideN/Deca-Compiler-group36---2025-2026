package fr.ensimag.deca.tree;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

public class TestProgram {

    @Mock
    ListDeclClass listDeclClass;
    @Mock
    AbstractMain main;

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);
    }

    @Test
    public void testProgramWithMainOnly() throws ContextualError{
        Program program = new Program(listDeclClass, main);
        program.verifyProgram(compiler);

        // Uncomment when the call to verifyListClass is implemented
        // verify(listDeclClass).verifyListClass(compiler);
        
        verify(main).verifyMain(compiler);
    }
}
