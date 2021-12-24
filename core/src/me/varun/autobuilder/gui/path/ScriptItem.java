package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.events.input.InputEventThrower;
import me.varun.autobuilder.events.input.TextChangeListener;
import me.varun.autobuilder.gui.elements.TextBox;
import me.varun.autobuilder.scripting.Parser;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ScriptItem extends AbstractGuiItem implements TextChangeListener {
    private static final Color LIGHT_BLUE = Color.valueOf("86CDF9");

    TextBox textBox;
    boolean error = true;

    public ScriptItem(@NotNull InputEventThrower inputEventThrower) {
        textBox = new TextBox("", inputEventThrower, true, this, 22);
    }

    public ScriptItem(@NotNull InputEventThrower inputEventThrower, String text, boolean closed, boolean valid) {

        textBox = new TextBox(text, inputEventThrower, true, this, 22);
        error = !valid;
        this.setClosed(closed);
    }

    @Override
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, PathGui pathGui) {
        super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, pathGui);
        if (isClosed()) {
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    LIGHT_BLUE, "Script", error);
            return 40;
        } else {
            int height = (int) (textBox.getHeight() + 8);
            shapeRenderer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5, (drawStartY - 40) - height, drawWidth - 5, height + 5,
                    2);

            textBox.draw(shapeRenderer, spriteBatch, drawStartX + 10, drawStartY - 43, drawWidth - 15);
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    LIGHT_BLUE, "Script", error);

            return height + 40;
        }
    }


    @Override
    public void onTextChange(String text, TextBox textBox) {

        error = !Parser.execute(text, AutoBuilder.getConfig().getScriptMethods());

    }

    @Override
    public String onTextBoxClick(String text, TextBox textBox) {
        return text;
    }

    @Override
    public void dispose() {
        textBox.dispose();
    }

    @Override
    public String toString() {
        return "ScriptItem{" +
                "textBox=" + textBox +
                '}';
    }

    public String getText() {
        return textBox.getText();
    }

    public boolean isValid() {
        return !error;
    }
}
