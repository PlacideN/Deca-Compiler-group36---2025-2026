package fr.ensimag.deca.codegen;

import fr.ensimag.ima.pseudocode.Label;

/**
 * Manager for unique labels used in control flow statements (if, while).
 * 
 * @author gl36
 * @date 06/01/2026
 */
public class LabelManager {
    private int ifCount = 0;
    private int boolOpCount = 0;
    private int whileCount = 0;

    public int getNextIfLabel() {
        return ifCount++;
    }

    public int getNextBoolOpLabel() {
        return boolOpCount++;
    }

    public int getNextWhileLabel() {
        return whileCount++;
    }

    public Label ifEndLabel(int ifNumber) {
        return new Label("IF_END_" + ifNumber);
    }

    public Label ifElseLabel(int ifNumber) {
        return new Label("IF_ELSE_" + ifNumber);
    }

    public Label boolOpEndLabel(int boolOpNumber) {
        return new Label("BOOL_OP_END_" + boolOpNumber);
    }

    public Label whileStartLabel(int whileNumber) {
        return new Label("WHILE_BEGIN_" + whileNumber);
    }

    public Label whileEndLabel(int whileNumber) {
        return new Label("WHILE_END_" + whileNumber);
    }

    public Label runtimeErrorLabel(String error) {
        return new Label(error + "_error");
    }
}
