package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import org.mockito.internal.stubbing.answers.Returns;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl36
 * @date 01/01/2026
 */
public abstract class AbstractLValue extends AbstractExpr {
    /**
     * Returns the definition of the Lvalue.
     * Used for code generation (Step C) when retrieving the address of the Lvalue.
     * @return the definition of the expression as an ExpDefinition.
     */
    public abstract ExpDefinition getExpDefinition();
}
