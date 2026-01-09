package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.POP;

/**
 * Manager for the allocation and deallocation of general purpose registers during code generation.
 * 
 * @author gl36
 * @date 06/01/2026
 */
public class RegisterManager {
    
    private final int maxRegisterIndex;
    private int nextFree = 2; // R0 and R1 are reserved

    private int currentTempStackUsage = 0;
    private int maxTempStackUsage = 0;
    private final DecacCompiler compiler;

    /**
     * Constructor of the RegisterManager.
     * @param compiler 
     * @param allowedRegisterCount the number of registers that can be used (4 to 16)
     */
    public RegisterManager(DecacCompiler compiler, int allowedRegisterCount) {
        this.compiler = compiler;
        this.maxRegisterIndex = allowedRegisterCount - 1; 
    }

    /**
     * Allocate a new register.
     * 
     * @return the allocated general purpose register
     */
    public GPRegister allocateRegister() {
        if (nextFree <= maxRegisterIndex) {
            return GPRegister.getR(nextFree++);
        } else {
            GPRegister last = GPRegister.getR(maxRegisterIndex);
            compiler.addInstruction(new PUSH(last)); // save the content of the last register on the stack
            currentTempStackUsage++;
            if (currentTempStackUsage > maxTempStackUsage) {
                maxTempStackUsage = currentTempStackUsage;
            }
            return last;
        }
    }

    /**
     * Free the last allocated register.
     */
    public void freeRegister(GPRegister reg) {
        if (currentTempStackUsage > 0 && reg.getNumber() == maxRegisterIndex) {
            compiler.addInstruction(new POP(reg));
            currentTempStackUsage--;
        } else if (nextFree > 2) {
            nextFree--;
        }
    }

    /**
     * Get the number of the next free register.
     * @return the number of the next free register
     */
    public int getNextFreeRegister() {
        return nextFree;
    }

    /**
     * Get the maximum temporary stack usage of the program due to register spilling.
     * @return the maximum number of temporary stack slots used during code generation
     */  
    public int getMaxTempStackUsage() {
        return maxTempStackUsage;
    }

}
