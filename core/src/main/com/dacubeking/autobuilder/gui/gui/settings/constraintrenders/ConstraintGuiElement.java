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
import com.dacubeking.autobuilder.gui.util.Colors;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.SwerveDriveKinematicsConstraint;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A {@link GuiElement} that renders a gui to edit {@link TrajectoryConstraint}s.
 * <p>
 * This supports adding, removing, and editing constraints.
 *
 * @implNote The Trajectory Constraints all have {@link Constraint} and the fields we want to render have {@link ConstraintField}
 * annotations. The annotations contain the name and description fields that are displayed.
 * <p>
 * When the list of constraints is received we then search though all the fields on the class and then recursively go through all
 * the annotated fields until we land on a double filed that can be rendered with a textbox. (Each recursion depth become an
 * indent level.)
 * <p>
 * We then create a callback on the textbox that uses reflection to set the field on the constraint (and then updates a bunch of
 * other stuff to make sure everything stays in the correct state).  (+ some special cases for null Trajectory States that shows
 * the trajectory chooser and arrays.)
 */
public class ConstraintGuiElement implements GuiElement {

    private final Supplier<List<TrajectoryConstraint>> constraintsSupplier;

    /**
     * @param constraintsSupplier A supplier that returns the list of constraints to render.
     * @param alwaysShowAddButton Whether to always show the add button.
     */
    public ConstraintGuiElement(Supplier<List<TrajectoryConstraint>> constraintsSupplier, boolean alwaysShowAddButton) {
        this.constraintsSupplier = constraintsSupplier;
        addConstraintGuiElement = new AddConstraintGuiElement((trajectoryConstraint) -> {
            constraintsSupplier.get().add(trajectoryConstraint);
            UndoHandler.getInstance().somethingChanged();
            // We're adding an element, so flush unsaved changes in our undo history
            reloadConstraintsGui();
            flushChanges();
        }, !alwaysShowAddButton);
    }

    private final List<GuiElement> constraints = new ArrayList<>();

    private boolean constraintsGuiReloadWanted = false;

    private final GuiElement spaceBetweenConstraints = new DividerGuiElement();

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

    /**
     * Updates the constraints to match the underlying constraints from the constraints supplier.
     */
    private void updateConstraintsRenderers() {
        for (GuiElement constraint : constraints) {
            constraint.dispose();
        }
        constraints.clear();
        List<TrajectoryConstraint> trajectoryConstraints = constraintsSupplier.get();
        for (int i = 0; i < trajectoryConstraints.size(); i++) {
            TrajectoryConstraint constraint = trajectoryConstraints.get(i);
            if (constraint.getClass().isAnnotationPresent(Constraint.class)) {
                Constraint constraintClass = constraint.getClass().getAnnotation(Constraint.class);
                final int finalI = i; // We need to make a final copy of i for the lambda

                // Create a TextGuiElement that shows the name of the constraint + some other info
                constraints.add(new TextGuiElement(new TextComponent(constraintClass.name(), Color.BLACK)
                        .setBold(true).setSize(20))
                        .setHoverText(new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent(constraintClass.description(), Color.BLACK),
                                new TextComponent("\n\nClick to remove this constraint", Color.RED).setBold(true)))
                        .setOnClick(() -> {
                            constraintsSupplier.get().remove(finalI); // Remove the constraint using the index of the constraint
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
            // Loop through all the fields on the constraint and search for fields that have the ConstraintField annotation
            if (field.isAnnotationPresent(ConstraintField.class)) {
                ConstraintField constraintAnnotation = field.getAnnotation(ConstraintField.class);

                // Create a textComponent that has the name of the field and a description from the annotation
                var labelText = new TextComponent(constraintAnnotation.name(), Color.BLACK).setBold(false);
                var labelHover = new TextBlock(Fonts.ROBOTO, 14, 300,
                        new TextComponent(constraintAnnotation.description(), Color.BLACK));

                if (field.getType().equals(double.class)) {
                    // Render the double as a labeled number text box
                    field.setAccessible(true);
                    try {
                        // Create a text box that has the value of the field
                        NumberTextBox textBox = new NumberTextBox(String.valueOf(field.getDouble(constraint)), true, 16);
                        LabeledTextInputField labeledInputField = new LabeledTextInputField(labelText, textBox, 100f);

                        labeledInputField.setHoverText(labelHover); // Set the hover text as the description from the annotation
                        textBox.setTextChangeCallback((t) -> {
                            try {
                                double number = Double.parseDouble(t.getText());
                                field.setDouble(constraint, number);
                                // We have to do this because the text box could be editing a field that other fields depend on
                                updateConstraints();
                                updatePaths();
                                UndoHandler.getInstance().somethingChanged();
                                labeledInputField.setValid(true);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e); // We set the field to be accessible, so this should never happen
                            } catch (NumberFormatException e) {
                                // We couldn't parse the number, so we'll just ignore it and tell the user it's invalid
                                labeledInputField.setValid(false);
                            }
                        });

                        elementsToIndent.add(labeledInputField); // Add the labeled input field to the list of elements to indent
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e); // We set the field to be accessible, so this should never happen
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
                                // Create a textComponent that has the name of the field and a description from the annotation
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
                            // We can't directly render this field, so we'll render the field's name and then recursively
                            // render the fields of the field
                            if (field.get(constraint).getClass().isAnnotationPresent(Constraint.class)) {
                                // This is a constraint, so we'll render the name of the constraint with 1 extra indent
                                var labelText1 = new TextComponent(field.get(constraint).getClass()
                                        .getAnnotation(Constraint.class).name(), Color.BLACK).setBold(false);
                                var labelHover1 = new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent(field.get(constraint).getClass()
                                                .getAnnotation(Constraint.class).description(), Color.BLACK),
                                        new TextComponent("\n\nClick to remove this constraint", Color.RED).setBold(true));
                                TextGuiElement header = new TextGuiElement(labelText1.setBold(true)).setHoverText(labelHover1);
                                elementsToIndent.add(header);
                                header.setOnClick(() -> {
                                    try {
                                        // Set the field to null. This will allow the add constraint button to show up
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
                            elementsToIndent.add(getConstraintFields(field.get(constraint), indentLevel + 1)); //recurse
                        } else {
                            if (field.getType().equals(TrajectoryConstraint.class)) {
                                // Render a button that lets the user add a new constraint
                                elementsToIndent.add(new AddConstraintGuiElement((c) -> {
                                    try {
                                        field.set(constraint, c);
                                        UndoHandler.getInstance().somethingChanged();
                                        // We're adding an element, so flush unsaved changes in our undo history
                                        flushChanges();
                                        reloadConstraintsGui();
                                    } catch (IllegalAccessException e) {
                                        // We set the field to be accessible, so this should never happen
                                        throw new RuntimeException(e);
                                    }
                                }).setHighlightColor(highlightColor));
                            } else {
                                elementsToIndent.add(new TextGuiElement(new TextComponent("null", Color.BLACK)));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e); // We set the field to be accessible, so this should never happen
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

    private final AddConstraintGuiElement addConstraintGuiElement;

    /**
     * Calls the update functions of all the constraints.
     * <p>
     * Needed for the constraints to update their values when the user changes a field.
     * <p>
     * Ex. The {@link SwerveDriveKinematicsConstraint} needs to update its kinematic matrix when any of the kinematic parameters
     * are changed.
     */
    private void updateConstraints() {
        for (TrajectoryConstraint trajectoryConstraint : constraintsSupplier.get()) {
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

        // Only call the update functions here to prevent changing the constraints while they're being rendered
        // (and prevent weird bugs)
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
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement constraint : constraints) {
            drawY -= 5 + constraint.getHeight(drawStartX, drawY, drawWidth, isLeftMouseJustUnpressed);
        }
        drawY -= 5 + addConstraintGuiElement.getHeight(drawStartX, drawY, drawWidth, isLeftMouseJustUnpressed);
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

    private Color highlightColor = Colors.LIGHT_GREY;

    /**
     * Requires a reload of the constraint gui to apply to the current state of the constraints
     */
    public void setHighlightColor(Color color) {
        addConstraintGuiElement.setHighlightColor(color);
        this.highlightColor = color;
    }
}
