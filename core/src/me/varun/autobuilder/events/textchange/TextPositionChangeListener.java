package me.varun.autobuilder.events.textchange;

public interface TextPositionChangeListener {
    /**
     * Fired when text in a textbox is changed
     * @param text new text
     * @param row row specified when creating the object
     * @param column column specified when creating the object
     */
    void onTextChange(String text, int row, int column);
}
