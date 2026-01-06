package fr.ensimag.deca.codegen;

/**
 * Manager for the allocation of global variables on the stack (GB and LB)
 * LB management is not implemented yet.
 * 
 * @author gl36
 * @date 06/01/2026
 */
public class StackManager {
    private int currentGBOffset = 0;

    /**
     * Allocate a new global variable on the stack.
     * 
     * @return the word offset of the allocated GB
     */
    public int allocate() {
        return currentGBOffset++;
    }

    // No deallocation needed for GB as they live for the entire duration of the program
}
