package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.LabeledTextInputField;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.TextGuiElement;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.DifferentialDriveKinematics;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.function.Consumer;

public class DifferentDriveKinematicsGuiElement extends TrajectoryConstraintGuiElement {

    private final TextGuiElement title = new TextGuiElement(new TextComponent("Kinematics", Color.BLACK).setBold(true)
            .setSize(H_2_FONT_SIZE));
    private final LabeledTextInputField trackWidthMetersInputField;
    private final Consumer<DifferentialDriveKinematics> onKinematicsChange;

    public DifferentDriveKinematicsGuiElement(DifferentialDriveKinematics kinematics,
                                              Consumer<DifferentialDriveKinematics> onKinematicsChange) {
        super(-1);
        trackWidthMetersInputField = new LabeledTextInputField(
                new TextComponent("Track Width: ", Color.BLACK).setBold(false),
                new NumberTextBox(
                        String.valueOf(kinematics.trackWidthMeters),
                        true, (this::updateTrackWidthMeters), (TextBox::getText), 16), 100f);
        this.onKinematicsChange = onKinematicsChange;
    }

    private void updateTrackWidthMeters(TextBox textBox) {
        try {
            onKinematicsChange.accept(new DifferentialDriveKinematics(Double.parseDouble(textBox.getText())));
            trackWidthMetersInputField.setValid(true);
        } catch (NumberFormatException e) {
            trackWidthMetersInputField.setValid(false);
        }
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float startY = drawStartY;
        startY -= renderTitle(title, shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, camera);
        startY -= trackWidthMetersInputField.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera,
                isLeftMouseJustUnpressed) + 5;
        return drawStartY - startY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        return getTitleHeight(title, drawStartX, drawStartY, drawWidth) +
                trackWidthMetersInputField.getHeight(drawStartX, drawStartY, drawWidth, camera, isLeftMouseJustUnpressed) + 5;
    }
}
