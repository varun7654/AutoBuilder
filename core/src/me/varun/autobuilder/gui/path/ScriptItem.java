package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.gui.elements.TextBox;
import me.varun.autobuilder.scripting.Parser;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ScriptItem extends AbstractGuiItem implements TextChangeListener {
    private static final Color LIGHT_BLUE = Color.valueOf("86CDF9");

    static {
    }

    private final ShaderProgram fontShader;
    private final BitmapFont font;
    TextBox textBox;
    boolean error = true;

    public ScriptItem(@NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower inputEventThrower) {
        this.fontShader = fontShader;
        this.font = font;

        textBox = new TextBox("", fontShader, font, inputEventThrower, true, this);
    }

    public ScriptItem(@NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower inputEventThrower,
                      String text, boolean closed, boolean valid) {
        this.fontShader = fontShader;
        this.font = font;

        textBox = new TextBox(text, fontShader, font, inputEventThrower, true, this);
        error = !valid;
        this.setClosed(closed);
    }

    @Override
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, PathGui pathGui) {
        super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, pathGui);
        if (isClosed()) {
            renderHeader(shapeRenderer, spriteBatch, fontShader, font, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture, LIGHT_BLUE, "Script", error);
            return 40;
        } else {
            int height = (int) (textBox.getHeight(drawWidth - 15, 20) + 8);
            shapeRenderer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5, (drawStartY - 40) - height, drawWidth - 5, height + 5, 2);

            renderHeader(shapeRenderer, spriteBatch, fontShader, font, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture, LIGHT_BLUE, "Script", error);

            spriteBatch.setShader(fontShader);
            font.setColor(Color.BLACK);
            textBox.draw(shapeRenderer, spriteBatch, drawStartX + 10, drawStartY - 43, drawWidth - 15, 20);
            spriteBatch.setShader(null);

            return height + 40;
        }
    }


    @Override
    public void onTextChange(String text, TextBox textBox) {

        error = !Parser.execute(text, AutoBuilder.getConfig().getScriptMethods());

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
