package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.scripting.Parser;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.events.input.TextChangeListener;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.scripting.sendable.SendableScript;
import com.dacubeking.autobuilder.gui.scripting.util.LintingPos;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ScriptItem extends AbstractGuiItem implements TextChangeListener {
    private static final Color LIGHT_BLUE = Color.valueOf("86CDF9");

    TextBox textBox;
    boolean error = true;

    final ArrayList<LintingPos> linting = new ArrayList<>();
    final ArrayList<LintingPos> synchronizedLinting = new ArrayList<>();

    CompletableFuture<SendableScript> latestSendableScript;

    public ScriptItem(@NotNull InputEventThrower inputEventThrower) {
        textBox = new TextBox("", inputEventThrower, true, this, 16);
    }

    public ScriptItem(@NotNull InputEventThrower inputEventThrower, String text, boolean closed, boolean valid) {
        textBox = new TextBox(text, inputEventThrower, true, this, 16);
        error = !valid;
        this.setClosed(closed);

        onTextChange(text, textBox);
    }

    @Override
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX, int drawStartY,
                      int drawWidth, PathGui pathGui, boolean isLeftMouseJustUnpressed) {
        super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, pathGui, isLeftMouseJustUnpressed);
        if (isClosed()) {
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    LIGHT_BLUE, "Script", error);
            return 40;
        } else {
            synchronized (linting) {
                synchronizedLinting.clear();
                synchronizedLinting.addAll(linting);
            }

            int height = (int) (textBox.getHeight() + 8);
            shapeRenderer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5, (drawStartY - 40) - height, drawWidth - 5, height + 5,
                    2);

            textBox.draw(shapeRenderer, spriteBatch, drawStartX + 10, drawStartY - 43, drawWidth - 15, synchronizedLinting);
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    LIGHT_BLUE, "Script", error);

            return height + 40;
        }
    }


    @Override
    public void onTextChange(String text, TextBox textBox) {
        CompletableFuture.supplyAsync(() -> {
            try {
                ArrayList<LintingPos> lintingPositions = new ArrayList<>();
                SendableScript sendableScript = new SendableScript();
                error = Parser.execute(text, lintingPositions, sendableScript);
                synchronized (linting) {
                    linting.clear();
                    linting.addAll(lintingPositions);
                }
                return sendableScript;
            } catch (Exception e) {
                AutoBuilder.handleCrash(e);
            }
            return null; // Will never happen (will crash before this)
        }, AutoBuilder.asyncParsingService);


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

    public SendableScript getSendableScript() {
        try {

            if (latestSendableScript != null) {
                return latestSendableScript.get();
            } else { // If the future is null, it means that the script has not been validated yet
                SendableScript sendableScript = new SendableScript();
                Parser.execute(textBox.getText(), new ArrayList<>(), sendableScript);
                return sendableScript;
            }
        } catch (InterruptedException | ExecutionException e) {
            AutoBuilder.handleCrash(e);
        }
        return null; // Will never happen (will crash before this)
    }
}
