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
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.CentripetalAccelerationConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class CentripetalAccelerationConstraintGuiElement extends TrajectoryConstraintGuiElement {

    private final LabeledTextInputField labeledTextInputField;

    private void updateMaxCentripetalAcceleration(TextBox textBox) {
        try {
            updateConstraint(new CentripetalAccelerationConstraint(Double.parseDouble(textBox.getText())));
            labeledTextInputField.setValid(true);
        } catch (NumberFormatException e) {
            labeledTextInputField.setValid(false);
        }
    }

    private static final TextGuiElement titleText = new TextGuiElement(
            new TextComponent("Centripetal Acceleration Constraint", Color.BLACK).setBold(true).setSize(H_1_FONT_SIZE));

    public CentripetalAccelerationConstraintGuiElement(CentripetalAccelerationConstraint centripetalAccelerationConstraint,
                                                       int i, ConstraintsGuiElement constraintsGuiElement) {
        super(i);
        labeledTextInputField = new LabeledTextInputField(
                new TextComponent("Max Acceleration: ", Color.BLACK).setBold(false),
                new NumberTextBox(
                        String.valueOf(centripetalAccelerationConstraint.getMaxCentripetalAccelerationMetersPerSecondSq()),
                        true, (this::updateMaxCentripetalAcceleration), (TextBox::getText), 16), 100f);
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float startY = drawStartY;
        startY -= renderTitle(titleText, shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera);
        RoundedShapeRenderer.roundedRectTopLeft(shapeRenderer, drawStartX + 5, startY + 2, drawWidth - 10,
                labeledTextInputField.getHeight(drawStartX, drawStartY, drawWidth, camera, isLeftMouseJustUnpressed) + 6,
                5f, Colors.LIGHT_GREY);
        startY -= labeledTextInputField.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera,
                isLeftMouseJustUnpressed);
        return drawStartY - startY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float startY = drawStartY;
        startY -= titleText.getHeight(drawStartX, startY, drawWidth, camera, isLeftMouseJustUnpressed);
        startY -= labeledTextInputField.getHeight(drawStartX, startY, drawWidth, camera, isLeftMouseJustUnpressed);
        return drawStartY - startY;
    }
}
