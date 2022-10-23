package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.GuiElement;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.TextGuiElement;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.util.Colors;
import com.dacubeking.autobuilder.gui.util.MiscShapeRenderer;
import com.dacubeking.autobuilder.gui.util.MouseUtil;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import com.dacubeking.autobuilder.gui.wpi.math.controller.SimpleMotorFeedforward;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Rotation2d;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Translation2d;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.DifferentialDriveKinematics;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.MecanumDriveKinematics;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.SwerveDriveKinematics;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.*;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.isMouseOver;

public class AddConstraintGuiElement implements GuiElement {

    private static final List<ConstraintType> constraints = new ArrayList<>();

    static {
        constraints.add(new ConstraintType("Centripetal Acceleration",
                () -> new CentripetalAccelerationConstraint(1.0)));
        constraints.add(new ConstraintType("Differential Drive Kinematics",
                () -> new DifferentialDriveKinematicsConstraint(new DifferentialDriveKinematics(10), 1.0)));
        constraints.add(new ConstraintType("Differential Drive Voltage",
                () -> new DifferentialDriveVoltageConstraint(new SimpleMotorFeedforward(1, 1, 1),
                        new DifferentialDriveKinematics(10), 10.0)));
        constraints.add(new ConstraintType("Elliptical Region",
                () -> new EllipticalRegionConstraint(new Translation2d(), 1, 1, new Rotation2d(), new MaxVelocityConstraint(1))));
        constraints.add(new ConstraintType("Max Velocity",
                () -> new MaxVelocityConstraint(1.0)));
        constraints.add(new ConstraintType("Mecanum Drive Kinematics",
                () -> new MecanumDriveKinematicsConstraint(new MecanumDriveKinematics(
                        new Translation2d(), new Translation2d(), new Translation2d(), new Translation2d()), 1.0)));
        constraints.add(new ConstraintType("Rectangular Drive Kinematics",
                () -> new RectangularRegionConstraint(new Translation2d(), new Translation2d(), new MaxVelocityConstraint(1))));
        constraints.add(new ConstraintType("Swerve Drive Kinematics",
                () -> new SwerveDriveKinematicsConstraint(new SwerveDriveKinematics(
                        new Translation2d(), new Translation2d(), new Translation2d(), new Translation2d()), 1)));
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float startY = drawStartY;
        for (ConstraintType constraint : constraints) {
            startY -= constraint.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera,
                    isLeftMouseJustUnpressed);
        }
        return drawStartY - startY;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float height = 0;
        for (ConstraintType constraint : constraints) {
            height += constraint.getHeight(drawStartX, drawStartY, drawWidth, camera, isLeftMouseJustUnpressed);
        }
        return height;
    }

    record ConstraintType(String name, Supplier<TrajectoryConstraint> trajectoryConstraintSupplier,
                          TextGuiElement textGuiElement) {
        public ConstraintType(String name, Supplier<TrajectoryConstraint> trajectoryConstraintSupplier) {
            this(name, trajectoryConstraintSupplier, new TextGuiElement(new TextComponent(name)));
        }

        public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                            float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
            float textHeight = textGuiElement.getHeight(drawStartX + 20, drawStartY, drawWidth - 20, camera,
                    isLeftMouseJustUnpressed);
            if (isMouseOver(drawStartX, drawStartY - 4 - textHeight, drawWidth, textHeight + 8)) {
                RoundedShapeRenderer.roundedRectTopLeft(shapeRenderer, drawStartX,
                        drawStartY + 5, drawWidth, textHeight + 10, 5, Colors.LIGHT_GREY);

                if (MouseUtil.isIsLeftMouseJustUnpressed()) {
                    AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints.add(trajectoryConstraintSupplier.get());
                    UndoHandler.getInstance().reloadState();
                }
            }

            MiscShapeRenderer.plusIconCentered(shapeRenderer, drawStartX + 15, drawStartY - textHeight / 2, 16, 15,
                    Color.BLACK);


            textGuiElement.render(shapeRenderer, spriteBatch, drawStartX + 20, drawStartY, drawWidth - 20, camera,
                    isLeftMouseJustUnpressed);

            return textHeight + 10;
        }

        public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera,
                               boolean isLeftMouseJustUnpressed) {
            return textGuiElement.getHeight(drawStartX + 20, drawStartY, drawWidth - 20, camera, isLeftMouseJustUnpressed) + 10;
        }
    }
}
