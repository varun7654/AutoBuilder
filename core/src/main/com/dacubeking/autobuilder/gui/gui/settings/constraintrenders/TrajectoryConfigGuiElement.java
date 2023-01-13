package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.GuiElement;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.LabeledTextInputField;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.SpaceGuiElement;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public class TrajectoryConfigGuiElement implements GuiElement {
    private final LabeledTextInputField maxVelocityTextField = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Max Velocity: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond),
                    true, (this::updateMaxVelocity), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The maximum velocity of the robot in meters per second.", Color.BLACK)));

    private final LabeledTextInputField maxAccelerationTextField = (LabeledTextInputField) new LabeledTextInputField(
            new TextComponent("Max Acceleration: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxAccelerationMetersPerSecondSq),
                    true, (this::updateMaxAcceleration), (TextBox::getText), 16),
            100f)
            .setHoverText(HoverManager.createDefaultHoverTextBlock(
                    new TextComponent("The maximum acceleration of the robot in meters per second squared.", Color.BLACK)));

    private final ConstraintGuiElement constraintGuiElement =
            new ConstraintGuiElement(() -> AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints, true);

    private void updateMaxVelocity(@NotNull TextBox textBox) {
        try {
            AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond = Double.parseDouble(textBox.getText());
            maxVelocityTextField.setValid(true);
            updatePathRenders();
        } catch (NumberFormatException e) {
            maxVelocityTextField.setValid(false);
        }
    }

    private void updateMaxAcceleration(@NotNull TextBox textBox) {
        try {
            AutoBuilder.getConfig().getPathingConfig().maxAccelerationMetersPerSecondSq = Double.parseDouble(textBox.getText());
            maxAccelerationTextField.setValid(true);
            updatePathRenders();
        } catch (NumberFormatException e) {
            maxAccelerationTextField.setValid(false);
        }
    }

    private static void updatePathRenders() {
        AutoBuilder.getInstance().pathGui.updatePaths();
        AutoBuilder.requestRendering();
    }


    ArrayList<GuiElement> elements = new ArrayList<>();

    {
        elements.add(maxVelocityTextField);
        elements.add(maxAccelerationTextField);
        elements.add(new SpaceGuiElement(15f));
        elements.add(constraintGuiElement);
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                    isLeftMouseJustUnpressed) + 3;
        }


        return drawStartY - drawY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.getHeight(drawStartX, drawY, drawWidth, isLeftMouseJustUnpressed) + 3;
        }

        return drawStartY - drawY;
    }

    @Override
    public void onUnfocus() {
        for (GuiElement element : elements) {
            element.onUnfocus();
        }
    }

    @Override
    public void dispose() {
        for (GuiElement element : elements) {
            element.dispose();
        }
    }

    public void updateValues() {
        maxVelocityTextField.textBox.setText(
                String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond));
        maxAccelerationTextField.textBox.setText(
                String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxAccelerationMetersPerSecondSq));
        constraintGuiElement.updateValues();
    }
}
