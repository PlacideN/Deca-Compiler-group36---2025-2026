package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current" 
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current" 
 * dictionary and in the parentEnvironment if it fails. 
 * 
 * Insertion (through method declare) is always done in the "current" dictionary.
 * 
 * @author gl36
 * @date 01/01/2026
 */
public class EnvironmentExp {

    EnvironmentExp parentEnvironment;
    // On crée un dictionnaire pour contenir le symbole de l'environnement courant à laquelle on associe son environnement parent
    Private Map<Symbol, ExpDefinition> environment;
    
    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
        this.environment = new HashMap<>();
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public ExpDefinition get(Symbol key) {
        if (environment.containsKey(key)) {
            return environment.get(key);
        }
        if (environment.parentEnvironment != null) {
            // Appel récursif sur le parent de l'environnement 
            return parentEnvironment.get(key);
        }
        // si ni l'environnement contient la clef, ni tous ses parents, on renvoie null
        return null;
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary 
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (environment.containsKey(name)) {
            throw new DoubleDefException();
        }
        environment.put(name, def);
    }

}
