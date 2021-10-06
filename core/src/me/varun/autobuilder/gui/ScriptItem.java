package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.gui.elements.TextBox;
import me.varun.autobuilder.scriptengine.*;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.invoke.WrongMethodTypeException;

public class ScriptItem extends AbstractGuiItem implements TextChangeListener {
    private static final Color LIGHT_BLUE = Color.valueOf("86CDF9");
    static ScriptEngineManager manager;
    static ScriptEngine engine;

    static {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");

        engine.put("shooter", new Shooter());
        engine.put("hopper", Hopper.getInstance());
        engine.put("intake", Intake.getInstance());
        engine.put("visionManager", VisionManager.getInstance());

        engine.put("auto", new TemplateAuto(new Translation2D()));
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
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, Gui gui) {
        super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, gui);
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
        try {
            engine.eval(text);
            error = false;
        } catch (ScriptException | WrongMethodTypeException exception) {
            error = true;
        } catch (Exception e) {
            System.out.println("Unexpected Exception");
            e.printStackTrace();
            error = true;
        }
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
