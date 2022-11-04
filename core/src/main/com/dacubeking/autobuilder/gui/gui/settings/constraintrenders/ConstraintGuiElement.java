package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.*;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public class ConstraintGuiElement implements GuiElement {
    private final ArrayList<GuiElement> constraints = new ArrayList<>();

    private boolean constraintsGuiReloadWanted = false;

    private final SpaceGuiElement spaceBetweenConstraints = new SpaceGuiElement(10f);

    /**
     * Reloads the auto and config and updates the constraints GUI
     */
    public void reloadConstraintsGui() {
        constraintsGuiReloadWanted = true;
    }

    private boolean flushChangesWanted = false;

    public void flushChanges() {
        flushChangesWanted = true;
    }

    private void updateConstraintsRenderers() {
        for (GuiElement constraint : constraints) {
            constraint.dispose();
        }
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
                            UndoHandler.getInstance().somethingChanged();
                            flushChanges();
                            reloadConstraintsGui();
                            System.out.println("Removed constraint " + finalI);
                        }));
                constraints.add(getConstraintFields(constraint, 1));
                constraints.add(spaceBetweenConstraints);
            }
        }
    }

    @Contract("_, _ -> new")
    private @NotNull IndentedElement getConstraintFields(@NotNull Object constraint, int indentLevel) {
        ArrayList<GuiElement> elementsToIndent = new ArrayList<>();
        for (var field : constraint.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConstraintField.class)) {
                var constraintAnnotation = field.getAnnotation(ConstraintField.class);
                var labelText = new TextComponent(constraintAnnotation.name(), Color.BLACK).setBold(false);
                var labelHover = new TextBlock(Fonts.ROBOTO, 14, 300,
                        new TextComponent(constraintAnnotation.description(), Color.BLACK));

                if (field.getType().equals(double.class)) {
                    // Render the double as a labeled number text box
                    field.setAccessible(true);
                    try {
                        NumberTextBox textBox = new NumberTextBox(String.valueOf(field.getDouble(constraint)), true, 16);
                        LabeledTextInputField labeledInputField = new LabeledTextInputField(labelText, textBox, 100f);
                        labeledInputField.setHoverText(labelHover);
                        textBox.setTextChangeCallback((t) -> {
                            try {
                                double number = Double.parseDouble(t.getText());
                                field.setDouble(constraint, number);
                                updateConstraints();
                                // We have to do this because the text box could be editing a field that other
                                // fields depend on
                                updatePaths();
                                UndoHandler.getInstance().somethingChanged();
                                labeledInputField.setValid(true);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            } catch (NumberFormatException e) {
                                labeledInputField.setValid(false);
                            }
                        });

                        elementsToIndent.add(labeledInputField);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else if (field.getType().isArray()) {
                    // Render the individual elements of the array
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
                                // Render the name of each element in the array with 1 extra indent
                                elementsToIndent2.add(new TextGuiElement(labelText1.setBold(true)).setHoverText(labelHover1));
                                // Render the fields of each element in the array with 2 extra indents
                                elementsToIndent2.add(getConstraintFields(o, indentLevel + 2));
                            }
                        }
                        elementsToIndent.add(new IndentedElement(indentLevel + 1, elementsToIndent2));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        field.setAccessible(true);
                        if (field.get(constraint) != null) {
                            if (field.get(constraint).getClass().isAnnotationPresent(Constraint.class)) {
                                var labelText1 = new TextComponent(field.get(constraint).getClass()
                                        .getAnnotation(Constraint.class).name(), Color.BLACK).setBold(false);
                                var labelHover1 = new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent(field.get(constraint).getClass()
                                                .getAnnotation(Constraint.class).description(), Color.BLACK),
                                        new TextComponent("\n\nClick to remove this constraint", Color.RED));

                                TextGuiElement header = new TextGuiElement(labelText1.setBold(true)).setHoverText(labelHover1);
                                elementsToIndent.add(header);
                                header.setOnClick(() -> {
                                    try {
                                        field.set(constraint, null);
                                        // We're deleting an element, so flush unsaved changes in our undo history
                                        reloadConstraintsGui();
                                        UndoHandler.getInstance().somethingChanged();
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            } else {
                                elementsToIndent.add(new TextGuiElement(labelText.setBold(true)).setHoverText(labelHover));
                            }
                            elementsToIndent.add(getConstraintFields(field.get(constraint), indentLevel + 1));
                        } else {
                            if (field.getType().equals(TrajectoryConstraint.class)) {
                                elementsToIndent.add(new AddConstraintGuiElement((c) -> {
                                    try {
                                        field.set(constraint, c);
                                        UndoHandler.getInstance().somethingChanged();
                                        // We're adding an element, so flush unsaved changes in our undo history
                                        flushChanges();
                                        reloadConstraintsGui();
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

    boolean updatePaths = false;

    private void updatePaths() {
        updatePaths = true;
    }

    private final AddConstraintGuiElement addConstraintGuiElement =
            new AddConstraintGuiElement((trajectoryConstraint) -> {
                AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints.add(trajectoryConstraint);
                UndoHandler.getInstance().somethingChanged();
                // We're adding an element, so flush unsaved changes in our undo history
                reloadConstraintsGui();
                flushChanges();
            }, false);

    private void updateConstraints() {
        for (TrajectoryConstraint trajectoryConstraint : AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints) {
            trajectoryConstraint.update();
        }
    }


    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement constraint : constraints) {
            drawY -= 5 + constraint.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                    isLeftMouseJustUnpressed);
        }

        drawY -= 5 + addConstraintGuiElement.render(shapeRenderer, spriteBatch, drawStartX, drawY, drawWidth, camera,
                isLeftMouseJustUnpressed);

        if (constraintsGuiReloadWanted) {
            constraintsGuiReloadWanted = false;
            UndoHandler.getInstance().reloadState();
            AutoBuilder.requestRendering();
        }

        if (updatePaths) {
            updatePaths = false;
            AutoBuilder.getInstance().pathGui.updatePaths();
            AutoBuilder.requestRendering();
        }

        if (flushChangesWanted) {
            UndoHandler.getInstance().flushChanges();
            flushChangesWanted = false;
        }
        return drawStartY - drawY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement constraint : constraints) {
            drawY -= 5 + constraint.getHeight(drawStartX, drawY, drawWidth, camera, isLeftMouseJustUnpressed);
        }
        return drawStartY - drawY;
    }

    @Override
    public void dispose() {
        for (GuiElement constraint : constraints) {
            constraint.dispose();
        }
    }

    public void updateValues() {
        updateConstraintsRenderers();
    }
}
