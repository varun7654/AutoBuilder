package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.events.input.TextChangeListener;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.notification.Notification;
import com.dacubeking.autobuilder.gui.gui.notification.NotificationHandler;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.scripting.Parser;
import com.dacubeking.autobuilder.gui.scripting.RobotCodeData;
import com.dacubeking.autobuilder.gui.scripting.sendable.SendableScript;
import com.dacubeking.autobuilder.gui.scripting.util.LintingPos;
import com.dacubeking.autobuilder.gui.util.MouseUtil;
import com.dacubeking.autobuilder.gui.util.OsUtil;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ScriptItem extends AbstractGuiItem implements TextChangeListener {

    private static final Texture openIcon = new Texture(Gdx.files.internal("open.png"), true);

    static {
        openIcon.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
    }

    private static final Color LIGHT_BLUE = Color.valueOf("86CDF9");

    TextBox textBox;
    boolean error = true;

    final ArrayList<LintingPos> linting = new ArrayList<>();
    final ArrayList<LintingPos> synchronizedLinting = new ArrayList<>();

    CompletableFuture<SendableScript> latestSendableScript;

    private static final TextComponent warningText =
            new TextComponent("You script contains errors. Hover over the text underlined in red to get more info.");

    public ScriptItem() {
        textBox = new TextBox("", true, this, 16);
    }

    public ScriptItem(String text, boolean closed, boolean valid) {
        textBox = new TextBox(text, true, this, 16);
        error = !valid;
        this.setInitialClosed(closed);

        onTextChange(text, textBox);
    }

    private final String tempAutoLocation = "C:\\Users\\varun\\Documents\\GitHub\\FRC-2022\\src\\main\\java\\tmp";

    @Override
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX, int drawStartY,
                      int drawWidth, PathGui pathGui, Camera camera, boolean isLeftMouseJustUnpressed) {
        int pop = super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, pathGui, camera,
                isLeftMouseJustUnpressed);
        if (isFullyClosed()) {
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    LIGHT_BLUE, "Script", error, warningText);
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
                    LIGHT_BLUE, "Script", error, warningText);

            if (MouseUtil.isMouseOver(drawStartX + drawWidth - 27, drawStartY - 61, 20, 20)) {
                spriteBatch.setColor(1, 1, 1, 1f);
                HoverManager.setHoverText(TextBlock.from("Open in VS Code"));
                if (MouseUtil.isIsLeftMouseJustUnpressed()) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            String randomFileName = "autoScript" + UUID.randomUUID().toString().replace("-", "");
                            File file = new File(tempAutoLocation + "\\" + randomFileName + ".java");
                            new File(tempAutoLocation).mkdirs();
                            file.createNewFile();
                            try (FileWriter fileWriter = new FileWriter(file)) {
                                StringBuilder instances = new StringBuilder();
                                StringBuilder imports = new StringBuilder();
                                for (String accessibleClass : RobotCodeData.accessibleClasses) {
                                    imports.append("import ").append(accessibleClass).append(";\n");
                                    String plainClass = getLast(accessibleClass.split("\\."));
                                    instances.append("         ").append(plainClass).append(" ")
                                            .append(makeFirstLower(plainClass))
                                            .append(" = AutonomousContainer.getInstance().getAccessibleInstances().get(\"")
                                            .append(plainClass).append("\");\n");
                                }

                                fileWriter.write(
                                        "package frc.tmp;" +
                                                "\n" +
                                                imports.toString() +
                                                "\n" +
                                                "public class " + randomFileName + " implements Runnable {\n" +
                                                "    @Override\n" +
                                                "    public void run() {\n" +
                                                "    //This code is automatically generated by the AutoBuilder.\n" +
                                                instances.toString() + "\n" +
                                                "    //Write your code here\n" +
                                                textBox.getText() + "\n" +
                                                "    }\n" +
                                                "}\n"
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (OsUtil.isWindows) {
                                Runtime.getRuntime().exec("cmd /c code " + file.getAbsolutePath());
                            } else {
                                Runtime.getRuntime().exec("sh -c code");
                            }
                        } catch (IOException e) {
                            NotificationHandler.addNotification(new Notification(Color.RED, "Failed to open VS Code!", 3000));
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                spriteBatch.setColor(1, 1, 1, 0.2f);
            }
            spriteBatch.draw(openIcon, drawStartX + drawWidth - 27, drawStartY - 61, 20, 20);
            spriteBatch.setColor(1, 1, 1, 1f);
        }
        spriteBatch.flush();
        if (pop == 1) ScissorStack.popScissors();
        return getHeight();
    }

    private String makeFirstLower(String string) {
        if (string.length() == 0) return "";
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    private String getLast(String[] array) {
        return array[array.length - 1];
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
        super.dispose();
        textBox.dispose();
    }

    @Override
    public int getOpenHeight() {
        return (int) (textBox.getHeight() + 8);
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
