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
    private int currentLBOffset = 0;

    /**
     * Allocate a new global variable on the stack.
     * 
     * @return the word offset of the allocated GB
     */
    public int allocate() {
        currentGBOffset++; // Stack starts at GB + 1
        return currentGBOffset;
    }

    /**
     * Allocate a new local variable on the stack.
     * @return the word offset of the allocated LB
     */
    public int allocateLB() {
        currentLBOffset++;
        return currentLBOffset;   
    }

    /**
     * Get the total number of slots (global variables) allocated on the stack.
     * @return the total number of slots allocated
     */
    public int getStackSize() {
        return currentGBOffset;
    }

    // No deallocation needed for GB as they live for the entire duration of the program

    /**
     * Get the current LB offset.
     * @return the current LB offset
     */
    public int getCurrentLBOffset() {
        return currentLBOffset;
    }

    /**
     * Reset the LB offset to 0.
     */
    public void resetLB() {
        currentLBOffset = 0;
    }

}
