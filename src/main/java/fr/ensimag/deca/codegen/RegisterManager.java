package fr.ensimag.deca.codegen;

import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Manager for the allocation and deallocation of general purpose registers during code generation.
 * 
 * @author gl36
 * @date 06/01/2026
 */
public class RegisterManager {
    
    private int maxRegisters = 15; 
    private int nextFree = 2; // R0 and R1 are reserved

    /**
     * Allocate a new register.
     * 
     * @return the allocated general purpose register
     */
    public GPRegister allocateRegister() {
        if (nextFree > maxRegisters) {
            throw new UnsupportedOperationException("Out of registers. Spilling not implemented yet.");
        }
        return GPRegister.getR(nextFree++);
    }

    /**
     * Free the last allocated register.
     */
    public void freeRegister() {
        if (nextFree > 2) {
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

}
