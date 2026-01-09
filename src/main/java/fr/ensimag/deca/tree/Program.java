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
        // TODO: call verify methods on classes
        this.main.verifyMain(compiler);
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        IMAProgram body = new IMAProgram();
        IMAProgram init = compiler.getProgram();
        compiler.setProgram(body);

        compiler.addComment("Main program");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());

        compiler.setProgram(init);

        int stackSize = compiler.getStackManager().getStackSize();
        if (!compiler.getCompilerOptions().getNoCheck()) {
            int tsto = compiler.getRegisterManager().getMaxTempStackUsage() + stackSize;
            if (tsto > 0) {
                compiler.addInstruction(new TSTO(tsto));
                Label errorLabel = compiler.getRuntimeErrorManager().getErrorLabel(ErrorType.STACK_OVERFLOW);
                compiler.addInstruction(new BOV(errorLabel));
            }
        }
        if (stackSize > 0) {
            compiler.addInstruction(new ADDSP(stackSize));
        }


        init.append(body);
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
