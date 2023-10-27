package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.GuiElement;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.TextGuiElement;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.Colors;
import com.dacubeking.autobuilder.gui.util.MiscShapeRenderer;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.constraint.*;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMousePos;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.isMouseOver;

/**
 * A {@link GuiElement} that renders a short gui that allows the user to add a constraint.
 */
public class AddConstraintGuiElement implements GuiElement {

    private static final List<ConstraintType> constraints = new ArrayList<>();
    protected final Consumer<TrajectoryConstraint> onAddConstraint;
    private final boolean allowClose;

    private Color highlightColor = Colors.LIGHT_GREY;

    /**
     * @param onAddConstraint The callback to call when a constraint is added.
     */
    public AddConstraintGuiElement(Consumer<TrajectoryConstraint> onAddConstraint) {
        this(onAddConstraint, true);
    }

    /**
     * @param onAddConstraint The callback to call when a constraint is added.
     * @param allowClose      Whether to allow the user to close the gui.
     */
    public AddConstraintGuiElement(Consumer<TrajectoryConstraint> onAddConstraint, boolean allowClose) {
        this.onAddConstraint = onAddConstraint;
        this.allowClose = allowClose;
        this.isExpanded = !allowClose;
    }


    private boolean isExpanded;
    ConstraintType header = new ConstraintType(new TextComponent("Add a Constraint").setBold(true).setSize(18),
            () -> null);

    static {
        constraints.add(new ConstraintType("Elliptical Region",
                () -> new EllipticalRegionConstraint(new Translation2d(), 1, 1, new Rotation2d(), null)));
        constraints.add(new ConstraintType("Rectangular Region",
                () -> new RectangularRegionConstraint(new Translation2d(), new Translation2d(), null)));

        constraints.add(new ConstraintType("Centripetal Acceleration",
                () -> new CentripetalAccelerationConstraint(1.0)));
        constraints.add(new ConstraintType("Max Velocity",
                () -> new MaxVelocityConstraint(1.0)));
        constraints.add(new ConstraintType("Max Acceleration",
                () -> new MaxAccelerationConstraint(1.0)));
        
        constraints.add(new ConstraintType("Differential Drive Kinematics",
                () -> new DifferentialDriveKinematicsConstraint(new DifferentialDriveKinematics(10), 1.0)));
        constraints.add(new ConstraintType("Differential Drive Voltage",
                () -> new DifferentialDriveVoltageConstraint(new SimpleMotorFeedforward(1, 1, 1),
                        new DifferentialDriveKinematics(10), 10.0)));

        constraints.add(new ConstraintType("Mecanum Drive Kinematics",
                () -> new MecanumDriveKinematicsConstraint(new MecanumDriveKinematics(
                        new Translation2d(), new Translation2d(), new Translation2d(), new Translation2d()), 1.0)));
        constraints.add(new ConstraintType("Swerve Drive Kinematics",
                () -> new SwerveDriveKinematicsConstraint(new SwerveDriveKinematics(
                        new Translation2d(), new Translation2d(), new Translation2d(), new Translation2d()), 1)));
        constraints.add(new ConstraintType("Swerve Drive Voltage",
                () -> new SwerveDriveVoltageConstraint(new SimpleMotorFeedforward(1, 1, 1),
                        10.0)));
    }

    Vector2 pos = new Vector2();

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float startY = drawStartY;
        if (allowClose) {
            getMousePos(pos);
        } else {
            pos.set(-1, -1); //Make it so that it won't be highlighted when the mouse hovers over it
        }
        startY -= header.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera, isLeftMouseJustUnpressed,
                (c) -> isExpanded = !isExpanded || !allowClose, pos, highlightColor);
        if (isExpanded) {
            for (ConstraintType constraint : constraints) {
                startY -= constraint.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera,
                        isLeftMouseJustUnpressed, onAddConstraint, getMousePos(pos), highlightColor);
            }
        }
        return drawStartY - startY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        float height = 0;
        height += header.getHeight(drawStartX, drawStartY, drawWidth, isLeftMouseJustUnpressed);
        if (isExpanded) {
            for (ConstraintType constraint : constraints) {
                height += constraint.getHeight(drawStartX, drawStartY, drawWidth, isLeftMouseJustUnpressed);
            }
        }
        return height;
    }

    record ConstraintType(Supplier<TrajectoryConstraint> trajectoryConstraintSupplier,
                          TextGuiElement textGuiElement, boolean renderPlus) {
        public ConstraintType(String name, Supplier<TrajectoryConstraint> trajectoryConstraintSupplier) {
            this(trajectoryConstraintSupplier, new TextGuiElement(new TextComponent(name)), true);
        }

        public ConstraintType(TextComponent textComponent, Supplier<TrajectoryConstraint> trajectoryConstraintSupplier) {
            this(trajectoryConstraintSupplier, new TextGuiElement(textComponent), false);
        }

        public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                            float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed,
                            Consumer<TrajectoryConstraint> onAddConstraint, Vector2 mousePos, Color highlightColor) {
            float textHeight = textGuiElement.getHeight(drawStartX + 25, drawStartY, drawWidth - 20,
                    isLeftMouseJustUnpressed);
            if (isMouseOver(mousePos, drawStartX, drawStartY - 9 - textHeight, drawWidth, textHeight + 8)) {
                RoundedShapeRenderer.roundedRectTopLeft(shapeRenderer, drawStartX + 6,
                        drawStartY, drawWidth - 6, textHeight + 10, 5, highlightColor);

                if (isLeftMouseJustUnpressed) {
                    AutoBuilder.requestRendering();
                    onAddConstraint.accept(trajectoryConstraintSupplier.get());
                }
            }

            if (renderPlus) {
                shapeRenderer.setDefaultLineWidth(2);
                MiscShapeRenderer.plusIconCentered(shapeRenderer, drawStartX + 20, drawStartY - (textHeight / 2) - 5, 16, 15,
                        Color.BLACK);
                textGuiElement.render(shapeRenderer, spriteBatch, drawStartX + 20, drawStartY - 5, drawWidth - 20, camera,
                        isLeftMouseJustUnpressed);
            } else {
                textGuiElement.render(shapeRenderer, spriteBatch, drawStartX, drawStartY - 5, drawWidth, camera,
                        isLeftMouseJustUnpressed);
            }

            return textHeight + 10;
        }

        public float getHeight(float drawStartX, float drawStartY, float drawWidth,
                               boolean isLeftMouseJustUnpressed) {
            return textGuiElement.getHeight(drawStartX + 25, drawStartY, drawWidth - 20, isLeftMouseJustUnpressed) + 10;
        }
    }

    public AddConstraintGuiElement setHighlightColor(Color color) {
        this.highlightColor = color;
        return this;
    }

    @Override
    public void dispose() {
        for (ConstraintType constraint : constraints) {
            constraint.textGuiElement.dispose();
        }
    }
}
