package me.varun.autobuilder.events.textchange;

import me.varun.autobuilder.gui.elements.NumberTextBox;

public interface NumberTextboxChangeListener {
    /**
     * Fired when text in a textbox is changed
     *
     * @param text          new text
     * @param row           row specified when creating the object
     * @param column        column specified when creating the object
     * @param numberTextBox object that called this
     */
    void onTextChange(String text, int row, int column, NumberTextBox numberTextBox);

    /**
     * Fired when text in a textbox is clicked
     *
     * @param text          text in the textbox
     * @param row           row specified when creating the object
     * @param column        column specified when creating the object
     * @param numberTextBox object that called this
     * @return text to set in the textbox
     */
    String onTextBoxClick(String text, int row, int column, NumberTextBox numberTextBox);
}
