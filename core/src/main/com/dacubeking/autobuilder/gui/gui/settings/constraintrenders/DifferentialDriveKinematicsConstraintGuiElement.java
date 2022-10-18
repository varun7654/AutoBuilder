package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.LabeledTextInputField;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.TextGuiElement;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.Colors;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.DifferentialDriveKinematics;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.DifferentialDriveKinematicsConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class DifferentialDriveKinematicsConstraintGuiElement extends TrajectoryConstraintGuiElement {

    private final @NotNull LabeledTextInputField labeledTextInputField;
    private @NotNull DifferentialDriveKinematicsConstraint constraint;
    private @NotNull DifferentDriveKinematicsGuiElement differentDriveKinematicsGuiElement;

    public DifferentialDriveKinematicsConstraintGuiElement(DifferentialDriveKinematicsConstraint constraint, int index) {
        super(index);
        labeledTextInputField = new LabeledTextInputField(
                new TextComponent("Max Velocity: ", Color.BLACK).setBold(false),
                new NumberTextBox(
                        String.valueOf(constraint.m_maxSpeedMetersPerSecond),
                        true, (this::updateMaxVelocity), (TextBox::getText), 16), 100f);
        this.constraint = constraint;
        differentDriveKinematicsGuiElement = new DifferentDriveKinematicsGuiElement(constraint.m_kinematics,
                (this::updateKinematics));
    }

    private void updateKinematics(DifferentialDriveKinematics kinematics) {
        constraint = new DifferentialDriveKinematicsConstraint(kinematics, constraint.m_maxSpeedMetersPerSecond);
        updateConstraint(constraint);
    }

    private void updateMaxVelocity(TextBox textBox) {
        try {
            updateConstraint(
                    new DifferentialDriveKinematicsConstraint(constraint.m_kinematics,
                            Double.parseDouble(textBox.getText())));
            labeledTextInputField.setValid(true);
        } catch (NumberFormatException e) {
            labeledTextInputField.setValid(false);
        }
    }

    TextGuiElement title = new TextGuiElement(
            new TextComponent("Differential Drive Kinematics Constraint", Color.BLACK).setBold(true).setSize(H_1_FONT_SIZE));

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float startY = drawStartY;
        startY -= renderTitle(title, shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, camera);

        RoundedShapeRenderer.roundedRectTopLeft(shapeRenderer, drawStartX + 5, startY + 2, drawWidth - 10,
                labeledTextInputField.getHeight(drawStartX, drawStartY, drawWidth, camera, isLeftMouseJustUnpressed) +
                        differentDriveKinematicsGuiElement.getHeight(drawStartX, drawStartY, drawWidth, camera,
                                isLeftMouseJustUnpressed) + 4,
                5f, Colors.LIGHT_GREY);

        startY -= labeledTextInputField.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera,
                isLeftMouseJustUnpressed);
        startY -= differentDriveKinematicsGuiElement.render(shapeRenderer, spriteBatch, drawStartX + 25, startY, drawWidth - 25,
                camera, isLeftMouseJustUnpressed);
        return drawStartY - startY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        return getTitleHeight(title, drawStartX, drawStartY, drawWidth) +
                labeledTextInputField.getHeight(drawStartX, drawStartY, drawWidth, camera, isLeftMouseJustUnpressed) +
                differentDriveKinematicsGuiElement.getHeight(drawStartX + 25, 0, drawWidth - 25,
                        camera, isLeftMouseJustUnpressed);
    }
}
