package com.dacubeking.autobuilder.gui.gui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.*;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.TrajectoryConfigGuiElement;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.net.NetworkTablesHelper;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.dacubeking.autobuilder.gui.util.MathUtil.dist2;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.*;

public class SettingsGui extends ScrollableGui implements Disposable {
    private final LabeledTextInputField teamNumberInputField = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Team Number: ", Color.BLACK).setBold(false),
            new TextBox(String.valueOf(AutoBuilder.getConfig().getTeamNumber()), true,
                    (this::updateTeamNumber), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The team number of the robot you are building for.\n", Color.BLACK),
                    new TextComponent("This is used to connect to the robot via NetworkTables.\n", Color.BLACK),
                    new TextComponent("You may also enter an IP Address in here instead of a Team Number.", Color.BLACK)
            ));

    private final LabeledTextInputField robotLengthField = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Robot Length: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getRobotLength()), true,
                    (this::updateRobotLength), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The length of the robot in meters.\n", Color.BLACK),
                    new TextComponent("This ", Color.BLACK),
                    new TextComponent("isn't ", Color.BLACK).setItalic(true),
                    new TextComponent("used for calculating the robot's trajectory. And is only used to display an outline of " +
                            "the robot. As a result is recommended that you include your robot's bumpers in this measurement."
                            , Color.BLACK)
            ));

    private final LabeledTextInputField robotWidthField = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Robot Width: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getRobotWidth()), true,
                    (this::updateRobotWidth), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The width of the robot in meters.\n", Color.BLACK),
                    new TextComponent("This ", Color.BLACK),
                    new TextComponent("isn't ", Color.BLACK).setItalic(true),
                    new TextComponent("used for calculating the robot's trajectory. And is only used to display an outline of " +
                            "the robot. As a result is recommended that you include your robot's bumpers in this measurement.",
                            Color.BLACK)
            ));

    private final LabeledTextInputField pointScaleFactorField = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Point Scale Factor: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getPointScaleFactor()), true,
                    (this::updatePointScaleFactor), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The scaling factor used to convert meters to pixels to be " +
                            "rendered on the GUI. This can be calculated by taking the width of the field in pixels and " +
                            "dividing it by width of the field in meters.", Color.BLACK)
            ));

    private final LabeledTextInputField originX = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Origin X: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getOriginX()), true,
                    (this::updateOriginX), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The X coordinate of the origin of the field in pixels on the image.\n", Color.BLACK),
                    new TextComponent("This is used to determine where (0,0) is on the field image", Color.BLACK)
            ));

    private final LabeledTextInputField originY = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Origin Y: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getOriginY()), true,
                    (this::updateOriginY), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The Y coordinate of the origin of the field in pixels on the image.\n", Color.BLACK),
                    new TextComponent("This is used to determine where (0,0) is on the field image", Color.BLACK)
            ));

    private final LabeledCheckbox isHolonomicCheckbox = (LabeledCheckbox) new LabeledCheckbox(
            new TextComponent("Is Holonomic: ", Color.BLACK).setBold(false),
            this::updateIsHolonomic, AutoBuilder.getConfig().isHolonomic())
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("Whether or not the robot is holonomic. (e.g swerve, or mecanum)\n", Color.BLACK),
                    new TextComponent("Enabling holonomic mode decouples the heading and velocity of the robot.", Color.BLACK)
            ));

    private final LabeledCheckbox networkTablesEnabledCheckbox = (LabeledCheckbox) new LabeledCheckbox(
            new TextComponent("NetworkTables Enabled: ", Color.BLACK).setBold(false),
            this::updateNetworkTablesEnabled, AutoBuilder.getConfig().isNetworkTablesEnabled())
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("Whether or not to connect to the robot's NetworkTables server.\n", Color.BLACK),
                    new TextComponent("Network Tables needs to be enabled to utilize any of the features that require being " +
                            "connected to the robot.\n", Color.BLACK),
                    new TextComponent("Disable this if you are not connected to the robot's network and are experiencing " +
                            "consistent frame-time spikes. ", Color.BLACK).setBold(true)
            ));

    TrajectoryConfigGuiElement trajectoryConfigGuiElement = new TrajectoryConfigGuiElement();

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
        guiItems.add(trajectoryConfigGuiElement);
        guiItems.add(new DividerGuiElement());
        guiItems.add(new SpaceGuiElement(25));
    }


    private float maxScrollPos = 0;

    public SettingsGui() {
        super(new SettingsGuiOpenIcon(), null);
    }

    public boolean update() {
        boolean panelWasOpen = panelOpen;
        super.update(maxScrollPos);
        if (panelOpen && !panelWasOpen) {
            trajectoryConfigGuiElement.updateValues();

            for (GuiElement guiItem : guiItems) {
                guiItem.onUnfocus();
            }
        }
        return panelOpen && (isMouseOver(panelX, panelY, panelWidth, panelHeight)
                || isMouseOver(openButton.getX(), openButton.getY(), openButton.getWidth(), openButton.getHeight()));
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
                        camera, isIsLeftMouseJustUnpressed()
                                && dist2(mouseDownPos, mousePos) < 10
                                && isMouseOver(panelX, panelY, panelWidth, panelHeight));
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
        trajectoryConfigGuiElement.updateValues();
    }


    Pattern ipAddrPattern = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");


    public void updateTeamNumber(TextBox textBox) {
        try {
            String teamNumberString = textBox.getText();
            teamNumberString = teamNumberString.strip();
            if (teamNumberString.length() > 0 && teamNumberString.length() <= 4) {
                int teamNumber = Integer.parseInt(teamNumberString);
                if (teamNumber >= 0 && teamNumber <= 9999) {
                    AutoBuilder.getConfig().setTeamNumber(teamNumberString);
                    teamNumberInputField.setValid(true);
                }
            } else if (ipAddrPattern.matcher(teamNumberString).matches()) {
                AutoBuilder.getConfig().setTeamNumber(teamNumberString);
                teamNumberInputField.setValid(true);
            } else if (teamNumberString.equals("localhost")) {
                AutoBuilder.getConfig().setTeamNumber(teamNumberString);
                teamNumberInputField.setValid(true);
            } else {
                teamNumberInputField.setValid(false);
            }
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
        UndoHandler.getInstance().reloadPaths();
    }

    public void updateNetworkTablesEnabled(boolean networkTablesEnabled) {
        AutoBuilder.getConfig().setNetworkTablesEnabled(networkTablesEnabled);
        NetworkTablesHelper.getInstance().setNTEnabled(networkTablesEnabled);
        UndoHandler.getInstance().somethingChanged();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (GuiElement guiItem : guiItems) {
            guiItem.dispose();
        }
    }
}
