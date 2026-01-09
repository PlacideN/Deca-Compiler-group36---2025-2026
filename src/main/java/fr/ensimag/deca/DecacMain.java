package fr.ensimag.deca;

import java.io.File;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl36
 * @date 01/01/2026
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);
    
    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }
        if (options.getPrintBanner()) {
            System.out.println("--------------------------------------------------");
            System.out.println("            Projet GL - Équipe n°36               ");
            System.out.println("--------------------------------------------------");
            System.exit(0);
        }
        if (options.getSourceFiles().isEmpty()) {
            System.err.println("No source file provided");
            options.displayUsage();
            System.exit(1);
        }
        if (options.getParallel()) {
            int nbr_threads = Runtime.getRuntime().availableProcessors();
            LOG.info("Compilation avec " + nbr_threads + " threads en parallèle.");
            
            // Crée un pool de threads avec le nombre spécifié de threads
            ExecutorService executor = Executors.newFixedThreadPool(nbr_threads);
            // Results contient la liste des futurs résultats de compilation
            List<Future<Boolean>> results = new ArrayList<>();
            
            for (File source : options.getSourceFiles()) {
                // on crée une tâche de compilation
                DecacCompiler compiler = new DecacCompiler(options, source);
                // Soumet la tâche au pool de threads
                Future<Boolean> result = executor.submit(() -> compiler.compile());
                // Ajoute le futur résultat à la liste
                results.add(result);
            }
            executor.shutdown();
            for (Future<Boolean> result : results) {
                try {
                    if (result.get()) {
                        // Result.get() lit une variable de Future et bloque
                        // jusqu'à ce que le résultat soit disponible
                        error = true;
                    }
                } catch (InterruptedException | ExecutionException e) {     
                    System.err.println("Error during compilation: " + e.getMessage());
                    error = true;
                }
            }


        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}
