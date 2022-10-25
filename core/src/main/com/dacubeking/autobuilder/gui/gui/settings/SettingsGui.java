package com.dacubeking.autobuilder.gui.gui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.IntegerNumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.*;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.ConstraintsGuiElement;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.net.NetworkTablesHelper;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

import static com.dacubeking.autobuilder.gui.util.MathUtil.dist2;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMousePos;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.isIsLeftMouseJustUnpressed;

public class SettingsGui extends ScrollableGui {
    private final LabeledTextInputField teamNumberInputField = new LabeledTextInputField(
            new TextComponent("Team Number: ", Color.BLACK).setBold(false),
            new IntegerNumberTextBox(String.valueOf(AutoBuilder.getConfig().getTeamNumber()), true,
                    (this::updateTeamNumber), (TextBox::getText), 16),
            100f);

    private final LabeledTextInputField robotLengthField = new LabeledTextInputField(
            new TextComponent("Robot Length: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getRobotLength()), true,
                    (this::updateRobotLength), (TextBox::getText), 16),
            100f);

    private final LabeledTextInputField robotWidthField = new LabeledTextInputField(
            new TextComponent("Robot Width: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getRobotWidth()), true,
                    (this::updateRobotWidth), (TextBox::getText), 16),
            100f);

    private final LabeledTextInputField pointScaleFactorField = new LabeledTextInputField(
            new TextComponent("Point Scale Factor: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getPointScaleFactor()), true,
                    (this::updatePointScaleFactor), (TextBox::getText), 16),
            100f);

    private final LabeledTextInputField originX = new LabeledTextInputField(
            new TextComponent("Origin X: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getOriginX()), true,
                    (this::updateOriginX), (TextBox::getText), 16),
            100f);

    private final LabeledTextInputField originY = new LabeledTextInputField(
            new TextComponent("Origin Y: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getOriginY()), true,
                    (this::updateOriginY), (TextBox::getText), 16),
            100f);

    private final LabeledCheckbox isHolonomicCheckbox = new LabeledCheckbox(
            new TextComponent("Is Holonomic: ", Color.BLACK).setBold(false),
            this::updateIsHolonomic, AutoBuilder.getConfig().isHolonomic());

    private final LabeledCheckbox networkTablesEnabledCheckbox = new LabeledCheckbox(
            new TextComponent("NetworkTables Enabled: ", Color.BLACK).setBold(false),
            this::updateNetworkTablesEnabled, AutoBuilder.getConfig().isNetworkTablesEnabled());

    ConstraintsGuiElement constraintsGuiElement = new ConstraintsGuiElement();

    private ArrayList<GuiElement> guiItems = new ArrayList<>();

    {
        guiItems.add(new TextGuiElement(new TextComponent("Settings", Color.BLACK).setBold(true).setSize(35)));
        guiItems.add(new TextGuiElement(new TextComponent("Basics", Color.BLACK).setBold(true).setSize(28)));
        guiItems.add(new DividerGuiElement());
        guiItems.add(teamNumberInputField);
        guiItems.add(robotLengthField);
        guiItems.add(robotWidthField);
        guiItems.add(isHolonomicCheckbox);
        guiItems.add(new SpaceGuiElement(10));

        guiItems.add(new TextGuiElement(new TextComponent("App Config", Color.BLACK).setBold(true).setSize(28)));
        guiItems.add(new DividerGuiElement());
        guiItems.add(pointScaleFactorField);
        guiItems.add(originX);
        guiItems.add(originY);
        guiItems.add(networkTablesEnabledCheckbox);
        guiItems.add(new SpaceGuiElement(10));

        guiItems.add(new TextGuiElement(new TextComponent("Pathing Config", Color.BLACK).setBold(true).setSize(28)));
        guiItems.add(new DividerGuiElement());
        guiItems.add(constraintsGuiElement);
        guiItems.add(new DividerGuiElement());
    }


    private float maxScrollPos = 0;

    public SettingsGui() {
        super(new SettingsGuiOpenIcon(), null);
    }

    public boolean update() {
        super.update(maxScrollPos);
        return panelOpen;
    }

    private final Vector2 mouseDownPos = new Vector2();

    public void render(ShapeDrawer shapeDrawer, PolygonSpriteBatch batch, Camera camera) {
        super.render(shapeDrawer, batch);
        if (panelOpen) {
            shapeDrawer.setColor(Color.WHITE);
            RoundedShapeRenderer.roundedRect(shapeDrawer, panelX, panelY, panelWidth, panelHeight, 5);
            Rectangle scissors = new Rectangle();

            ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
            boolean pop = ScissorStack.pushScissors(scissors);

            float yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;

            Vector2 mousePos = getMousePos();

            if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
                mouseDownPos.set(mousePos);
            }

            for (GuiElement guiItem : guiItems) {
                yPos = yPos - 3 - guiItem.render(shapeDrawer, batch, panelX, yPos, panelWidth,
                        camera, isIsLeftMouseJustUnpressed() && dist2(mouseDownPos, mousePos) < 10);
            }


            if (pop) {
                batch.flush();
                ScissorStack.popScissors();
            }

            maxScrollPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos - yPos;
        }
    }

    public void updateValues() {
        teamNumberInputField.textBox.setText(String.valueOf(AutoBuilder.getConfig().getTeamNumber()));
        robotLengthField.textBox.setText(String.valueOf(AutoBuilder.getConfig().getRobotLength()));
        robotWidthField.textBox.setText(String.valueOf(AutoBuilder.getConfig().getRobotWidth()));
        isHolonomicCheckbox.setCheckBox(AutoBuilder.getConfig().isHolonomic());

        pointScaleFactorField.textBox.setText(String.valueOf(AutoBuilder.getConfig().getPointScaleFactor()));
        originX.textBox.setText(String.valueOf(AutoBuilder.getConfig().getOriginX()));
        originY.textBox.setText(String.valueOf(AutoBuilder.getConfig().getOriginY()));
        networkTablesEnabledCheckbox.setCheckBox(AutoBuilder.getConfig().isNetworkTablesEnabled());
        constraintsGuiElement.updateValues();
    }

    public void updateTeamNumber(TextBox textBox) {
        try {
            int teamNumber = Integer.parseInt(textBox.getText());
            AutoBuilder.getConfig().setTeamNumber(teamNumber);
            teamNumberInputField.setValid(true);
        } catch (NumberFormatException e) {
            teamNumberInputField.setValid(false);
        }
    }

    public void updateRobotLength(TextBox textBox) {
        try {
            float length = Float.parseFloat(textBox.getText());
            AutoBuilder.getConfig().setRobotLength(length);
            robotLengthField.setValid(true);
        } catch (NumberFormatException e) {
            robotLengthField.setValid(false);
        }
    }

    public void updateRobotWidth(TextBox textBox) {
        try {
            float width = Float.parseFloat(textBox.getText());
            AutoBuilder.getConfig().setRobotWidth(width);
            robotWidthField.setValid(true);
        } catch (NumberFormatException e) {
            robotWidthField.setValid(false);
        }
    }

    public void updatePointScaleFactor(TextBox textBox) {
        try {
            float scaleFactor = Float.parseFloat(textBox.getText());
            AutoBuilder.getConfig().setPointScaleFactor(scaleFactor);
            pointScaleFactorField.setValid(true);
        } catch (NumberFormatException e) {
            pointScaleFactorField.setValid(false);
        }
    }

    public void updateOriginX(TextBox textBox) {
        try {
            float originX = Float.parseFloat(textBox.getText());
            AutoBuilder.getConfig().setOriginX(originX);
            this.originX.setValid(true);
        } catch (NumberFormatException e) {
            this.originX.setValid(false);
        }
    }

    public void updateOriginY(TextBox textBox) {
        try {
            float originY = Float.parseFloat(textBox.getText());
            AutoBuilder.getConfig().setOriginY(originY);
            this.originY.setValid(true);
        } catch (NumberFormatException e) {
            this.originY.setValid(false);
        }
    }

    public void updateIsHolonomic(boolean isHolonomic) {
        AutoBuilder.getConfig().setHolonomic(isHolonomic);
        UndoHandler.getInstance().reloadState();
    }

    public void updateNetworkTablesEnabled(boolean networkTablesEnabled) {
        AutoBuilder.getConfig().setNetworkTablesEnabled(networkTablesEnabled);
        if (networkTablesEnabled) {
            NetworkTablesHelper.getInstance().start(AutoBuilder.getInstance().hudRenderer,
                    AutoBuilder.getInstance().drawableRenderer);
        } else {
            NetworkTablesHelper.getInstance().disconnectClient();
        }
        UndoHandler.getInstance().somethingChanged();
    }
}
