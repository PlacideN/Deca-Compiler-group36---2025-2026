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
    private int maxUsedRegisterIndex = 1; // R0 and R1 are reserved
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
            GPRegister reg = GPRegister.getR(nextFree++);
            if (reg.getNumber() > maxUsedRegisterIndex) {
                maxUsedRegisterIndex = reg.getNumber();
            }
            return reg;
        } else {
            GPRegister last = GPRegister.getR(maxRegisterIndex);
            PUSH inst = new PUSH(last);
            inst.setComment("Register overflow");
            compiler.addInstruction(inst); // save the content of last on the stack
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
            POP inst = new POP(reg);
            inst.setComment("Restoring " + reg + " from stack");
            compiler.addInstruction(inst);
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

    /**
     * Reset the maximum temporary stack usage counter.
     */
    public void resetMaxTempStackUsage() {
        this.maxTempStackUsage = 0;
    }

    /**
     * Returns how many registers (R2 and up) were used.
     * If maxUsedRegisterIndex is 3, we used R2 and R3, so returns 2.
     */
    public int getNbSavedRegisters() {
        return Math.max(0, maxUsedRegisterIndex - 1);
    }

    /**
     * Save all used registers (in a block) onto the stack.
     */
    public void saveRegisters(DecacCompiler compiler) {
        for (int i = 2; i <= maxUsedRegisterIndex; i++) { 
            compiler.addInstruction(new PUSH(GPRegister.getR(i)));
        }
    }

    /**
     * Restore all used registers (in a block) from the stack.
     */
    public void restoreRegisters(DecacCompiler compiler) {
        for (int i = maxUsedRegisterIndex; i >= 2; i--) {
            compiler.addInstruction(new POP(GPRegister.getR(i)));
        }
    }

    /**
     * Reset the register manager to its initial state.
     */
    public void reset() {
        nextFree = 2;
        currentTempStackUsage = 0;
        maxTempStackUsage = 0;
        maxUsedRegisterIndex = 1;
    }

    /**
     * Records a big temporary increase in stack usage (e.g., for method parameters).
     * @param n the number of stack slots to increase by
     */
    public void trackMethodCallStackIncrease(int n) {
        this.currentTempStackUsage += n;
        if (this.currentTempStackUsage > this.maxTempStackUsage) {
            this.maxTempStackUsage = this.currentTempStackUsage;
        }
        // the params stay on the stack until the SUBSP, after which we call releaseMethodCallStack
    }

    /**
     * Release the temporary stack increase after a method call.
     * @param n the number of stack slots to decrease by
     */
    public void freeMethodStackIncrease(int n) {
        this.currentTempStackUsage -= n;
    }

}
