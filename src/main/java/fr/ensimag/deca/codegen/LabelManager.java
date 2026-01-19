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
    private int instanceOfCount = 0;
    private int castCount = 0;

    private Label currentMethodEndLabel;

    public void setCurrentMethodEndLabel(Label label) {
        this.currentMethodEndLabel = label;
    }

    public Label getCurrentMethodEndLabel() {
        return this.currentMethodEndLabel;
    }

    public int getNextIfLabel() {
        return ifCount++;
    }

    public int getNextBoolOpLabel() {
        return boolOpCount++;
    }

    public int getNextWhileLabel() {
        return whileCount++;
    }

    public int getNextInstanceOfLabel() {
        return instanceOfCount++;
    }

    public int getNextCastLabel() {
        return castCount++;
    }

    public Label castLoop(int n) {
        return new Label("cast_loop_" + n);
    }

    public Label castEnd(int n) {
        return new Label("cast_end_" + n);
    }

    public Label instanceOfLoop(int n) {
        return new Label("instanceof_loop_" + n);
    }

    public Label instanceOfTrue(int n) {
        return new Label("instanceof_true_" + n);
    }

    public Label instanceOfEnd(int n) {
        return new Label("instanceof_end_" + n);
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

    public Label getMethodEndLabel(String className, String methodName) {
        return new Label("fin." + className + "." + methodName);
    }

    public Label getObjectEqualsLabel() {
        return new Label("code.Object.equals");
    }

    public Label getInitLabel(String className) {
        return new Label("init." + className);
    }

}
