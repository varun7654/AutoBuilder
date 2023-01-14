package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A {@link GuiElement} that contains a label and a textbox.
 */
public class LabeledTextInputField extends TextGuiElement {
    public final TextBox textBox;
    public final float textBoxWidth;


    /**
     * @param label        The label of the text input field.
     * @param textBox      The textbox.
     * @param textBoxWidth The width of the textbox.
     */
    public LabeledTextInputField(TextComponent label, @NotNull TextBox textBox, float textBoxWidth) {
        super(label);
        this.textBox = textBox.setOutlineColor(Color.BLACK);
        this.textBoxWidth = textBoxWidth;
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        textBox.update(textBoxWidth);
        textBox.draw(shapeRenderer, spriteBatch, drawStartX + drawWidth - textBoxWidth - 10, drawStartY - 10, textBoxWidth, null);
        float labelHeight = super.render(shapeRenderer, spriteBatch,
                drawStartX, drawStartY - 9,
                drawWidth - textBoxWidth - 12, camera, isLeftMouseJustUnpressed);
        return Math.max(labelHeight + 5, textBox.getHeight() + 2);
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        textBox.update(textBoxWidth);
        return Math.max(super.getHeight(drawStartX, drawStartY - 9,
                        drawWidth - textBoxWidth - 12, isLeftMouseJustUnpressed) + 5,
                textBox.getHeight() + 2);
    }

    boolean valid = true;

    /**
     * Sets the validity of the text input field.
     * <p>
     * If valid is false, the text input field will be rendered red & italic.
     *
     * @param valid Whether the text input field is valid.
     */
    public void setValid(boolean valid) {
        if (valid != this.valid) {
            if (valid) {
                for (TextComponent textComponent : text.getTextComponents()) {
                    textComponent.setColor(Color.BLACK);
                    textComponent.setItalic(false);
                }
            } else {
                for (TextComponent textComponent : text.getTextComponents()) {
                    textComponent.setColor(Color.RED);
                    textComponent.setItalic(true);
                }
            }
            text.setDirty(); // Force a re-render. Since we're manipulating the text components directly, we need to do this.
            this.valid = valid;
        }
    }

    @Override
    public void onUnfocus() {
        textBox.onUnfocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        textBox.dispose();
    }

    public void setText(String text) {
        textBox.setText(text, true);
    }
}
