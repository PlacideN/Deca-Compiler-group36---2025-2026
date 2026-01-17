package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;

public class ListDeclParam extends TreeList<AbstractDeclParam> {

    @Override
    public void decompile(IndentPrintStream s) {
        boolean isParamFirst = true;
        for (AbstractDeclParam p : getList()) {
            p.decompile(s);
            if (!isParamFirst) {
                s.print(", ");
            } else {
                isParamFirst = false;
            }
        }
    }

    /**
     * Implements non-terminal "list_decl_param" of [SyntaxeContextuelle] in pass 2 (Rule 2.8)
     * @param compiler contains the "env_types" attribute
     * @return Signature corresponds to the "sig" attribute
     */
    public Signature verifyListParamType(DecacCompiler compiler) throws ContextualError {
        Signature sig = new Signature();
        for (AbstractDeclParam p : getList()) {
            Type paramType = p.verifyParamType(compiler);
            sig.add(paramType);
        }
        return sig;
    }

    /**
     * Implements non-terminal "list_decl_param" of [SyntaxeContextuelle] in pass 3 (Rule 3.12)
     * @param compiler contains the "env_types" attribute
     * @param parentEnv corresponds to the "env_exp_sup" attribute (class)
     * @return EnvironmentExp corresponds to the "env_exp" attribute (params)
     */
    public EnvironmentExp verifyListParam(DecacCompiler compiler, EnvironmentExp parentEnv) throws ContextualError {
        EnvironmentExp env = new EnvironmentExp(parentEnv);
        int index  = -3; // params start at offset -3, -2 is for 'this', -1 for return address
        for (AbstractDeclParam p : getList()) {
            p.verifyParamName(compiler, env, index);
            index--;
        }
        return env;
    }

}
