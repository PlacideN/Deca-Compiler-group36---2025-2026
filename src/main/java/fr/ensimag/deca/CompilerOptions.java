package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl36
 * @date 01/01/2026
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }
    
    public boolean getParse() {
        return parse;
    }

    public boolean getVerification() {
        return verification;
    }

    public int getRegisterCount() {
        return registerCount;
    }

    public boolean getNoCheck() {
        return noCheck;
    }

    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private boolean parse = false;
    private boolean verification = false;
    private boolean noCheck = false;
    private int registerCount = 16; // default = use all 16 registers R0...R15
    private List<File> sourceFiles = new ArrayList<File>();

    
    public void parseArgs(String[] args) throws CLIException {
        if (args.length == 0) {
            displayUsage();
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                switch (arg) {
                case "-b": // print banner
                    printBanner = true;
                    break;
                case "-p": // parse only
                    parse = true;
                    break;
                case "-v": // verify only
                    verification = true;
                    break;
                case "-P": // parallel compilation
                    parallel = true;
                    break;
                    case "-d": // increase debug level
                    debug++;
                    break;
                case "-n": // disable arithmetic and stack overflow runtime checks
                    noCheck = true;
                    break;
                case "-r": // set a custom number of registers
                    if (i + 1 < args.length) {
                        try {
                            int registerCount = Integer.parseInt(args[++i]);
                            if (registerCount < 4 || registerCount > 16) {
                                throw new CLIException("Invalid number of registers: " + registerCount + " (expected 4 to 16).");
                            }
                            this.registerCount = registerCount;
                        } catch (NumberFormatException e) {
                            throw new CLIException("Register count " + args[i] + " is not a valid integer.");
                        }
                    } else {
                        throw new CLIException("Missing register count after -r option.");
                    } 
                    break;
                default:
                    throw new CLIException("Unknown option: " + arg);
                }
            } else {
                if (arg.endsWith(".deca")){
                    File file = new File(arg).getAbsoluteFile();
                    if (!sourceFiles.contains(file)) { // don't compile the same file twice
                        sourceFiles.add(file);
                    }
                        
                } else {
                    throw new CLIException("Invalid file extension: " + arg + " (expected .deca)");
                }
            }
        }
        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }

        if (verification && parse) {
            throw new CLIException("Options -p and -v are cannot be used together.");
        }

        // throw new UnsupportedOperationException("not yet implemented");
    }

    protected void displayUsage() {
        PrintStream err = System.err;
        err.println("Usage: decac [[-p | -v] [-n] [-r <number>] [-d]* <.deca source files>] | [-b]");
        err.println("");
        err.println("OPTIONS:");
        err.println("  -b          : Display team banner.");
        err.println("  -p          : Run lexer and parser and stop.");
        err.println("                (Print decompiled program if program is syntactically valid).");
        err.println("  -v          : Run lexer, parser, and contextual verification and stop.");
        err.println("                (No output if program is valid).");
        err.println("  -n          : Disable arithmetic and stack overflow runtime checks.");
        err.println("  -r <number> : Use a custom number of registers for code generation (4 to 16).");
        err.println("  -d          : Enable debug trace. Use multiple times to increase verbosity.");
        err.println("                Repeat option for more traces (INFO, DEBUG, TRACE, ALL).");
        err.println("  -P          : Enable parallel compilation of source files.");
        err.println("");
        err.println("  Options '-p' and '-v' cannot be used together.");
    }
}
