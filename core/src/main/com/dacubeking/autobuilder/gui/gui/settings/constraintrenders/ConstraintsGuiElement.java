package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.TextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.*;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
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

    private boolean requestConstraintsUpdate = false;

    public void updateConstraints() {
        constraints.clear();
        var trajectoryConstraints = AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints;
        for (int i = 0; i < trajectoryConstraints.size(); i++) {
            var constraint = trajectoryConstraints.get(i);
            if (constraint.getClass().isAnnotationPresent(Constraint.class)) {
                var constraintClass = constraint.getClass().getAnnotation(Constraint.class);
                final int finalI = i;
                constraints.add(new TextGuiElement(new TextComponent(constraintClass.name(), Color.BLACK)
                        .setBold(true).setSize(20))
                        .setHoverText(new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent(constraintClass.description(), Color.BLACK),
                                new TextComponent("\n\nClick to remove", Color.RED)))
                        .setOnClick(() -> {
                            AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints.remove(finalI);
                            requestConstraintsUpdate = true;
                            System.out.println("Removed constraint " + finalI);
                        }));
                constraints.add(getConstraintFields(constraint, 1));
                constraints.add(spaceBetweenConstraints);
            }
        }
    }

    public IndentedElement getConstraintFields(@NotNull Object constraint, int indentLevel) {
        ArrayList<GuiElement> elementsToIndent = new ArrayList<>();
        for (var field : constraint.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConstraintField.class)) {
                var constraintAnnotation = field.getAnnotation(ConstraintField.class);
                var labelText = new TextComponent(constraintAnnotation.name(), Color.BLACK).setBold(false);
                var labelHover = new TextBlock(Fonts.ROBOTO, 14, 300,
                        new TextComponent(constraintAnnotation.description(), Color.BLACK));

                if (field.getType().equals(double.class)) {
                    field.setAccessible(true);
                    try {
                        elementsToIndent.add(new LabeledTextInputField(labelText,
                                new NumberTextBox(String.valueOf(field.getDouble(constraint)),
                                        true,
                                        (t) -> {
                                            try {
                                                field.setDouble(constraint, Double.parseDouble(t.getText()));
                                            } catch (IllegalAccessException e) {
                                                throw new RuntimeException(e);
                                            }
                                        },
                                        (TextBox::getText), 16),
                                100f)
                                .setHoverText(labelHover));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else if (field.getType().isArray()) {
                    elementsToIndent.add(new TextGuiElement(labelText.setBold(true)).setHoverText(labelHover));

                    try {
                        field.setAccessible(true);
                        Object[] array = (Object[]) field.get(constraint);
                        ArrayList<GuiElement> elementsToIndent2 = new ArrayList<>();
                        for (Object o : array) {
                            if (o.getClass().isAnnotationPresent(ConstraintField.class)) {
                                var constraintAnnotation1 = o.getClass().getAnnotation(ConstraintField.class);
                                var labelText1 = new TextComponent(constraintAnnotation1.name(), Color.BLACK).setBold(false);
                                var labelHover1 = new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent(constraintAnnotation1.description(), Color.BLACK));
                                elementsToIndent2.add(new TextGuiElement(labelText1.setBold(true)).setHoverText(labelHover1));
                                elementsToIndent2.add(getConstraintFields(o, indentLevel + 2));
                            }
                        }
                        elementsToIndent.add(new IndentedElement(indentLevel + 1, elementsToIndent2));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    TextGuiElement header = new TextGuiElement(labelText.setBold(true)).setHoverText(labelHover);
                    elementsToIndent.add(header);
                    try {
                        field.setAccessible(true);
                        if (field.get(constraint) != null) {
                            if (field.get(constraint).getClass().isAnnotationPresent(ConstraintField.class)) {
                                labelHover.addTextElement(new TextComponent("\n\nClick to remove this constraint", Color.RED));
                                header.setOnClick(() -> {
                                    try {
                                        field.set(constraint, null);
                                        requestConstraintsUpdate = true;
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                            elementsToIndent.add(getConstraintFields(field.get(constraint), indentLevel + 1));
                        } else {
                            if (field.getType().isAnnotationPresent(ConstraintField.class)) {
                                elementsToIndent.add(new AddConstraintGuiElement((c) -> {
                                    try {
                                        field.set(c, constraint);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));
                            } else {
                                elementsToIndent.add(new TextGuiElement(new TextComponent("null", Color.BLACK)));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return new IndentedElement(indentLevel, elementsToIndent);
    }

    ArrayList<GuiElement> elements = new ArrayList<>();

    {
        elements.add(maxVelocityTextField);
        elements.add(maxAccelerationTextField);
        elements.add(new SpaceGuiElement(15f));
    }

    private final AddConstraintGuiElement addConstraintGuiElement = new AddConstraintGuiElement();
    private final TextGuiElement addConstraintTextGuiElement = new TextGuiElement(
            new TextComponent("Add Constraint", Color.BLACK).setBold(true).setUnderlined(true).setUnderlineColor(Color.BLACK));

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
        }

        if (requestConstraintsUpdate) {
            requestConstraintsUpdate = false;
            updateConstraints();
            AutoBuilder.requestRendering();
        }

        drawY -= 5 + addConstraintTextGuiElement.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                isLeftMouseJustUnpressed);
        drawY -= 5 + addConstraintGuiElement.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                isLeftMouseJustUnpressed);


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
