package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.codegen.RuntimeErrorManager.ErrorType;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.info("verify Program: start");

        LOG.debug("verify Program: Passe 1 (Verify class hierarchy)");
        this.classes.verifyListClass(compiler);
        LOG.debug("verify Program: Passe 2 (Verify fields and method signatures)");
        this.classes.verifyListClassMembers(compiler);
        int vTableSize = compiler.getVTableManager().getNextGBSlot(); 
        compiler.getStackManager().setGBOffset(vTableSize);
        
        LOG.debug("verify Program: Passe 3 (Verify method bodies and main instructions)");
        this.classes.verifyListClassBody(compiler);
        this.main.verifyMain(compiler);

        LOG.info("verify Program: end");
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        LOG.info("codeGen Program: start");

        LOG.debug("codeGen Program: generating VTables");
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("       Construction des tables des methodes       ");
        compiler.addComment("--------------------------------------------------");
        compiler.getVTableManager().codeGenObjectVTable(compiler);
        classes.codeGenListVTable(compiler);

        int vTableSize = compiler.getVTableManager().getNextGBSlot() - 1;
        compiler.getStackManager().setGBOffset(vTableSize);

        IMAProgram body = new IMAProgram();
        IMAProgram head = compiler.getProgram();
        compiler.setProgram(body);
        
        LOG.debug("codeGen Program: generating main instructions");
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("            Code du programme principal           ");
        compiler.addComment("--------------------------------------------------");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        
        LOG.debug("codeGen Program: generating preamble (TSTO/ADDSP)");
        IMAProgram preamble = new IMAProgram();
        compiler.setProgram(preamble);
        
        int stackSize = compiler.getStackManager().getStackSize();
        if (!compiler.getCompilerOptions().getNoCheck()) {
            int nbSavedRegs = compiler.getRegisterManager().getNbSavedRegisters(); // registers saved for method calls
            int maxTemp = compiler.getRegisterManager().getMaxTempStackUsage(); // method call / expression spilling
            int tsto = nbSavedRegs + maxTemp + stackSize;
            if (tsto > 0) {
                compiler.addInstruction(new TSTO(tsto));
                Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.STACK_OVERFLOW);
                compiler.addInstruction(new BOV(errorLabel));
            }
        }
        if (stackSize > 0) {
            compiler.addInstruction(new ADDSP(stackSize));
        }

        IMAProgram full = new IMAProgram();
        full.append(preamble);
        full.append(head);
        full.append(body);
    
        compiler.setProgram(full);

        LOG.debug("codeGen Program: generating field initialization and methods");
        classes.codeGenListInit(compiler);
        classes.codeGenListMethods(compiler);
        compiler.getVTableManager().codeGenObjectEquals(compiler);
        compiler.getRuntimeErrorManager().generateErrorHandlers(); // generate error assembly code
        LOG.info("codeGen Program: end");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
