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
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.CentripetalAccelerationConstraint;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.DifferentialDriveKinematicsConstraint;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public class ConstraintsGuiElement implements GuiElement {
    public ConstraintsGuiElement() {
        updateConstraints();
    }

    private final LabeledTextInputField maxVelocityTextField = new LabeledTextInputField(
            new TextComponent("Max Velocity: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond),
                    true, (this::updateMaxVelocity), (TextBox::getText), 16),
            100f);

    private final LabeledTextInputField maxAccelerationTextField = new LabeledTextInputField(
            new TextComponent("Max Acceleration: ", Color.BLACK).setBold(false),
            new NumberTextBox(String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxAccelerationMetersPerSecondSq),
                    true, (this::updateMaxAcceleration), (TextBox::getText), 16),
            100f);

    private void updateMaxVelocity(@NotNull TextBox textBox) {
        try {
            AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond = Double.parseDouble(textBox.getText());
            maxVelocityTextField.setValid(true);
        } catch (NumberFormatException e) {
            maxVelocityTextField.setValid(false);
        }
    }

    private void updateMaxAcceleration(@NotNull TextBox textBox) {
        try {
            AutoBuilder.getConfig().getPathingConfig().maxAccelerationMetersPerSecondSq = Double.parseDouble(textBox.getText());
            maxAccelerationTextField.setValid(true);
        } catch (NumberFormatException e) {
            maxAccelerationTextField.setValid(false);
        }
    }

    ArrayList<GuiElement> constraints = new ArrayList<>();

    public void updateConstraints() {
        constraints.clear();
        var trajectoryConstraints = AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints;
        for (int i = 0; i < trajectoryConstraints.size(); i++) {
            TrajectoryConstraint trajectoryConstraint = trajectoryConstraints.get(i);
            if (trajectoryConstraint instanceof CentripetalAccelerationConstraint constraint) {
                constraints.add(new CentripetalAccelerationConstraintGuiElement(constraint, i, this));
            }

            if (trajectoryConstraint instanceof DifferentialDriveKinematicsConstraint constraint) {
                constraints.add(new DifferentialDriveKinematicsConstraintGuiElement(constraint, i));
            }
        }
    }

    ArrayList<GuiElement> elements = new ArrayList<>();

    {
        elements.add(maxVelocityTextField);
        elements.add(maxAccelerationTextField);
        elements.add(new SpaceGuiElement(15f));
    }

    private final SpaceGuiElement spaceBetweenConstraints = new SpaceGuiElement(10f);

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                    isLeftMouseJustUnpressed) + 3;
        }


        for (GuiElement constraint : constraints) {
            drawY -= 5 + constraint.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                    isLeftMouseJustUnpressed);
            drawY -= spaceBetweenConstraints.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                    isLeftMouseJustUnpressed);
        }

        return drawStartY - drawY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.getHeight(drawStartX, drawY, drawWidth, camera, isLeftMouseJustUnpressed) + 3;
        }

        for (GuiElement constraint : constraints) {
            drawY -= 5 + constraint.getHeight(drawStartX, drawY, drawWidth, camera, isLeftMouseJustUnpressed);
            drawY -= spaceBetweenConstraints.getHeight(drawStartX, drawY, drawWidth, camera, isLeftMouseJustUnpressed);
        }

        return drawStartY - drawY;
    }

    public void updateValues() {
        maxVelocityTextField.textBox.setText(
                String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond));
        maxAccelerationTextField.textBox.setText(
                String.valueOf(AutoBuilder.getConfig().getPathingConfig().maxAccelerationMetersPerSecondSq));
        updateConstraints();
    }
}
