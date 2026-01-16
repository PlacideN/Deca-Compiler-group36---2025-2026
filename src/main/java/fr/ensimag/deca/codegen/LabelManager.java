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

    
    private Label currentMethodEndLabel;

    public void setMethodEndLabel(Label label) {
        this.currentMethodEndLabel = label;
    }

    public Label getMethodEndLabel() {
        return this.currentMethodEndLabel;
    }
    

    public Label ifEndLabel(int ifNumber) {
        return new Label("if_end_" + ifNumber);
    }

    public Label ifElseLabel(int ifNumber) {
        return new Label("if_else_" + ifNumber);
    }

    public Label boolOpEndLabel(int boolOpNumber) {
        return new Label("bool_op_end_" + boolOpNumber);
    }

    public Label whileStartLabel(int whileNumber) {
        return new Label("while_begin_" + whileNumber);
    }

    public Label whileEndLabel(int whileNumber) {
        return new Label("while_end_" + whileNumber);
    }

    public Label runtimeErrorLabel(String error) {
        return new Label(error + "_error");
    }

    public Label getMethodLabel(String className, String methodName) {
        return new Label("code." + className + "." + methodName);
    }

    public Label getObjectEqualsLabel() {
        return new Label("code.Object.equals");
    }
}
