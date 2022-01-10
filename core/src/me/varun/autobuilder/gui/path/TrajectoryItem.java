package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.input.InputEventThrower;
import me.varun.autobuilder.events.input.NumberTextboxChangeListener;
import me.varun.autobuilder.events.pathchange.PathChangeListener;
import me.varun.autobuilder.gui.elements.CheckBox;
import me.varun.autobuilder.gui.elements.NumberTextBox;
import me.varun.autobuilder.gui.textrendering.FontRenderer;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.pathing.MovablePointRenderer;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.pathing.TimedRotation;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.spline.Spline.ControlVector;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;
import me.varun.autobuilder.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.varun.autobuilder.AutoBuilder.getConfig;

public class TrajectoryItem extends AbstractGuiItem implements PathChangeListener, NumberTextboxChangeListener {
    private static final DecimalFormat df = new DecimalFormat();

    static {
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(4);
        df.setMinimumIntegerDigits(1);
    }

    private @NotNull
    final PathRenderer pathRenderer;
    private final @NotNull List<List<NumberTextBox>> textBoxes = new ArrayList<>();
    private final @NotNull InputEventThrower eventThrower;
    private final @NotNull CameraHandler cameraHandler;
    private final @NotNull CheckBox checkBox = new CheckBox(0, 0, 30, 30);
    private final @NotNull NumberTextBox startVelocityTextBox;
    private final @NotNull NumberTextBox endVelocityTextBox;


    public TrajectoryItem(@NotNull PathGui pathGui, @NotNull InputEventThrower eventThrower,
                          @NotNull CameraHandler cameraHandler) {
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;
        ControlVectorList controlVectorList = new ControlVectorList();
        controlVectorList.add(new ControlVector(new double[]{0, 2, 0}, new double[]{0, 0, 0}));
        controlVectorList.add(new ControlVector(new double[]{2, 2, 0}, new double[]{2, 0, 0}));
        List<Rotation2d> rotation2dList = new ArrayList<>();
        rotation2dList.add(Rotation2d.fromDegrees(0));
        rotation2dList.add(Rotation2d.fromDegrees(0));

        this.pathRenderer = new PathRenderer(pathGui.getNextColor(), controlVectorList, rotation2dList, pathGui.executorService, 0, 0, new ArrayList<>());
        pathRenderer.setPathChangeListener(this);

        startVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityStart()), eventThrower, this, 0, 0, 18);
        endVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityEnd()), eventThrower, this, 0, 0, 18);
    }

    public TrajectoryItem(PathGui pathGui, @NotNull InputEventThrower eventThrower, @NotNull CameraHandler cameraHandler,
                          @NotNull ControlVectorList controlVectors, @NotNull List<TimedRotation> rotation2dList, boolean reversed,
                          Color color, boolean closed, float velocityStart, float velocityEnd, @NotNull List<TrajectoryConstraint> constraints) {
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;

        this.pathRenderer = new PathRenderer(color, controlVectors,
                rotation2dList.stream().map(TimedRotation::getRotation).collect(Collectors.toList()),
                pathGui.executorService, velocityStart, velocityEnd, constraints);
        pathRenderer.setReversed(reversed);
        pathRenderer.setPathChangeListener(this);

        this.setClosed(closed);

        startVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityStart()), eventThrower, this, 0, 0, 18);
        endVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityEnd()), eventThrower, this, 0, 0, 18);
    }


    @Override
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, PathGui pathGui) {
        super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, pathGui);
        String title;
        if (pathRenderer.getTrajectory() != null) {
            title = "Path - " + df.format(pathRenderer.getTrajectory().getTotalTimeSeconds()) + "s";
        } else {
            title = "Path - Calculating";
        }
        if (isClosed()) {
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    pathRenderer.getColor(), title, checkWarning(pathGui));
            return 40;
        } else {
            ControlVectorList controlVectors = pathRenderer.getControlVectors();
            shapeRenderer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5,
                    drawStartY - (30 * 3 + (controlVectors.size() * 60) + 40) - 8, drawWidth - 5,
                    30 * 3 + (controlVectors.size() * 60) + 13, 2);

            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    pathRenderer.getColor(), title, checkWarning(pathGui));

            if (pathRenderer.getSelectionPoint() >= 0) {
                RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5,
                        drawStartY - 42 - (pathRenderer.getSelectionPoint() + 1) * 30, drawWidth - 5, 31, 3, Color.DARK_GRAY);
            }

            // Draw all the control vectors
            for (int i = 0; i < textBoxes.size(); i++) {
                List<NumberTextBox> textBoxList = textBoxes.get(i);

                textBoxList.get(0).draw(shapeRenderer, spriteBatch, drawStartX + 10 + 0 * 123, drawStartY - 50 - i * 60,
                        120, null);
                textBoxList.get(1).draw(shapeRenderer, spriteBatch, drawStartX + 10 + 1 * 123, drawStartY - 50 - i * 60,
                        120, null);
                textBoxList.get(2).draw(shapeRenderer, spriteBatch, drawStartX + 10 + 2 * 123, drawStartY - 50 - i * 60,
                        120, null);
                textBoxList.get(3).draw(shapeRenderer, spriteBatch, drawStartX + 10 + 0 * 123, drawStartY - 50 - i * 60 - 30,
                        120, null);
                textBoxList.get(4).draw(shapeRenderer, spriteBatch, drawStartX + 10 + 1 * 123, drawStartY - 50 - i * 60 - 30,
                        120, null);

            }

            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (63 + controlVectors.size() * 60),
                    Fonts.ROBOTO, 22, new TextComponent("Start Velocity: ").setBold(true).setColor(Color.BLACK));
            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (63 + (controlVectors.size()) * 60 + 30),
                    Fonts.ROBOTO, 22, new TextComponent("End Velocity: ").setBold(true).setColor(Color.BLACK));
            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (3 + (controlVectors.size()) * 60 + 60),
                    Fonts.ROBOTO, 22, new TextComponent("Reversed: ").setBold(true).setColor(Color.BLACK));

            startVelocityTextBox.draw(shapeRenderer, spriteBatch,
                    drawStartX + 10 + 2 * 123, drawStartY - 50 - controlVectors.size() * 60,
                    120, null);

            endVelocityTextBox.draw(shapeRenderer, spriteBatch,
                    drawStartX + 10 + 2 * 123, drawStartY - 50 - (controlVectors.size()) * 60 - 30,
                    120, null);

            checkBox.setX(drawStartX + drawWidth - 35);
            checkBox.setY(drawStartY - 43 - (controlVectors.size() * 60) - 90);
            checkBox.checkHover();
            if (checkBox.checkClick()) {
                pathRenderer.setReversed(!pathRenderer.isReversed());
                pathRenderer.updatePath(false);
                UndoHandler.getInstance().somethingChanged();
            }
            checkBox.render(shapeRenderer, spriteBatch, pathRenderer.isReversed());


            return 40 + (30 * 3 + (controlVectors.size() * 60)) + 8;
        }

    }

    private static final double allowedAngleError = 1e-2;

    private boolean checkWarning(PathGui pathGui) {
        TrajectoryItem lastTrajectoryItem = pathGui.getLastPath();
        if (lastTrajectoryItem != null) {
            ControlVectorList lastControlVectors = lastTrajectoryItem.getPathRenderer().getControlVectors();
            ControlVector lastPoint = lastControlVectors.get(lastControlVectors.size() - 1);
            ControlVector nextPoint = pathRenderer.getControlVectors().get(0);
            if (getConfig().isHolonomic()) {
                return !((lastPoint.x[0] == nextPoint.x[0]) && (lastPoint.y[0] == nextPoint.y[0]));
            } else {
                double lastAngle = Math.atan2(lastPoint.y[1], lastPoint.x[1]);
                double nextAngle = Math.atan2(nextPoint.y[1], nextPoint.x[1]);

                if (Math.abs(lastAngle - nextAngle) < allowedAngleError) {
                    return !((lastPoint.x[0] == nextPoint.x[0]) && (lastPoint.y[0] == nextPoint.y[0]));
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public @NotNull PathRenderer getPathRenderer() {
        return pathRenderer;
    }

    @Override
    public void onPathChange() {
        for (List<NumberTextBox> textBox : textBoxes) {
            for (NumberTextBox box : textBox) {
                box.dispose();
            }
        }
        textBoxes.clear();
        for (int i = 0; i < pathRenderer.getControlVectors().size(); i++) {
            ControlVector controlVector = pathRenderer.getControlVectors().get(i);
            Rotation2d rotation = pathRenderer.getRotations().get(i);
            NumberTextBox xBox = new NumberTextBox(df.format(controlVector.x[0]), eventThrower, this, i, 0, 18);
            NumberTextBox yBox = new NumberTextBox(df.format(controlVector.y[0]), eventThrower, this, i, 1, 18);
            double rotationDegrees = getConfig().isHolonomic() ?
                    rotation.getDegrees() : Math.toDegrees(Math.atan2(controlVector.y[1], controlVector.x[1]));
            NumberTextBox rotationBox = new NumberTextBox(df.format(rotationDegrees), eventThrower, this, i, 2, 18);

            NumberTextBox xControlBox = new NumberTextBox(df.format(controlVector.x[1]), eventThrower, this, i, 3, 18);
            NumberTextBox yControlBox = new NumberTextBox(df.format(controlVector.y[1]), eventThrower, this, i, 4, 18);

            textBoxes.add(Arrays.asList(xBox, yBox, rotationBox, xControlBox, yControlBox));
        }
    }

    @Override
    public void onTextChange(String text, int row, int column, NumberTextBox numberTextBox) {
        UndoHandler.getInstance().somethingChanged();
        if (numberTextBox == startVelocityTextBox) {
            try {
                float parsedNumber = Float.parseFloat(text);
                getPathRenderer().setVelocityStart(parsedNumber);
                pathRenderer.updatePath(false);
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        if (numberTextBox == endVelocityTextBox) {
            try {
                float parsedNumber = Float.parseFloat(text);
                getPathRenderer().setVelocityEnd(parsedNumber);
                pathRenderer.updatePath(false);
            } catch (NumberFormatException ignored) {
            }
            return;
        }


        try {
            double parsedNumber = Double.parseDouble(text);

            ControlVector controlVector = pathRenderer.getControlVectors().get(row);
            MovablePointRenderer point = pathRenderer.getPointList().get(row);
            switch (column) {
                case 0:
                    pathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{parsedNumber, controlVector.x[1], controlVector.x[2]},
                            new double[]{controlVector.y[0], controlVector.y[1], controlVector.y[2]}));
                    point.setX((float) parsedNumber);
                    break;
                case 1:
                    pathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], controlVector.x[1], controlVector.x[2]},
                            new double[]{parsedNumber, controlVector.y[1], controlVector.y[2]}));
                    point.setY((float) parsedNumber);
                    break;
                case 2:
                    if (getConfig().isHolonomic()) {
                        pathRenderer.getRotations().set(row, Rotation2d.fromDegrees(parsedNumber));
                    } else {
                        Vector2 v = new Vector2((float) controlVector.y[1], (float) controlVector.x[1]);
                        v.setAngleDeg((float) parsedNumber);
                        pathRenderer.getControlVectors().set(row, new ControlVector(
                                new double[]{controlVector.x[0], v.x, controlVector.x[2]},
                                new double[]{controlVector.y[0], v.y, controlVector.y[2]}));

                    }
                    break;
                case 3:
                    pathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], parsedNumber, controlVector.x[2]},
                            new double[]{controlVector.y[0], controlVector.y[1], controlVector.y[2]}));
                    break;
                case 4:
                    pathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], controlVector.x[1], controlVector.x[2]},
                            new double[]{controlVector.y[0], parsedNumber, controlVector.y[2]}));
            }

            cameraHandler.ensureOnScreen(point.getRenderPos3());
            pathRenderer.updatePath(false);


        } catch (NumberFormatException ignored) {
        }

    }

    @Override
    public String onTextBoxClick(String text, int row, int column, NumberTextBox numberTextBox) {
        if (numberTextBox == startVelocityTextBox || numberTextBox == endVelocityTextBox) {
            return text;
        }

        cameraHandler.ensureOnScreen(pathRenderer.getPointList().get(row).getRenderPos3());;
        return text;
    }

    @Override
    public void dispose() {
        for (List<NumberTextBox> textBox : textBoxes) {
            for (NumberTextBox box : textBox) {
                box.dispose();
            }
        }
    }

    @Override
    public String toString() {
        return "TrajectoryItem{" +
                "pathRenderer=" + pathRenderer +
                '}';
    }
}
