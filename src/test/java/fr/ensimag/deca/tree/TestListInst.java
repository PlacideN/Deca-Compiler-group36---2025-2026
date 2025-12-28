package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

public class TestListInst {
    @Mock
    AbstractInst inst1;
    @Mock
    AbstractInst inst2;

    DecacCompiler compiler;
    EnvironmentExp env;
    ListInst listInst;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        compiler = new DecacCompiler(null, null);
        env = new EnvironmentExp(null);

        listInst = new ListInst();
    }

    @Test
    public void testCallAllInstOfListInst() throws Exception {
        listInst.add(inst1);
        listInst.add(inst2);

        listInst.verifyListInst(compiler, env, null, compiler.environmentType.VOID);

        // Check is all the instructions were verified
        verify(inst1).verifyInst(compiler, env, null, compiler.environmentType.VOID);
        verify(inst2).verifyInst(compiler, env, null, compiler.environmentType.VOID);
    }

    @Test
    public void testEmptyListInst(){
        assertDoesNotThrow(() -> {
            listInst.verifyListInst(compiler, env, null, compiler.environmentType.VOID);
        }, 
        "Should not throw exception for empty ListInst");    
    }
}
