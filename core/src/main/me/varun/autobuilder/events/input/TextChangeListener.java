package me.varun.autobuilder.events.input;

import me.varun.autobuilder.gui.elements.TextBox;

public interface TextChangeListener {
    /**
     * Fired when text in a textbox is changed
     *
     * @param text    new text
     * @param textBox textbox that called this
     */
    void onTextChange(String text, TextBox textBox);

    /**
     * Fired when text in a textbox is clicked
     *
     * @param text    text in the textbox
     * @param textBox textbox that called this
     * @return text to set in the textbox
     */
    String onTextBoxClick(String text, TextBox textBox);
}
