package fr.ensimag.deca.codegen;

import java.util.LinkedHashSet;
import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

/**
 * Manager for runtime error code generation. Stores an ordered set of triggered runtime errors.
 * The LabelManager onlytracks the number of different runtime errors.
 * 
 * @author gl36
 * @date 06/01/2026
 */
public class RuntimeErrorManager {
    private final DecacCompiler compiler;
    private final LabelManager labelManager;
    private Set<ErrorType> triggeredErrors = new LinkedHashSet<>();

    public enum ErrorType {
        DIV_BY_ZERO("Error: Division by zero"),
        MOD_BY_ZERO("Error: Modulo by zero"),
        STACK_OVERFLOW("Error: Stack overflow"),
        IO("Error: Input/Output error"),
        OVERFLOW("Error: Overflow during arithmetic operation"),
        NO_RETURN("Error: No return statement in function with non-void return type");

        private final String message;

        ErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public RuntimeErrorManager(DecacCompiler compiler, LabelManager labelManager) {
        this.compiler = compiler;
        this.labelManager = labelManager;
    }

    /**
     * Get the label for a specific runtime error and add a new record if not already triggered.
     * 
     * @param errorType The type of runtime error
     * @return The ima pseudocode label associated with the runtime error
     */
    public Label getErrorLabel(ErrorType errorType) {
        triggeredErrors.add(errorType);
        return labelManager.runtimeErrorLabel(errorType.name().toLowerCase());
    }

    /**
     * Generate the error handling code for all triggered runtime errors.
     */
    public void generateErrorHandlers() {
        for (ErrorType type : triggeredErrors) {
            Label errorLabel = getErrorLabel(type);
            compiler.addLabel(errorLabel);
            compiler.addInstruction(new WSTR(type.getMessage()));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }
    }
}