package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
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
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        this.pathRenderer = new PathRenderer(pathGui.getNextColor(), controlVectorList, rotation2dList, pathGui.executorService, 0, 0);
        pathRenderer.setPathChangeListener(this);

        startVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityStart()), eventThrower, this, 0, 0, 18);
        endVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityEnd()), eventThrower, this, 0, 0, 18);
    }

    public TrajectoryItem(PathGui pathGui, @NotNull InputEventThrower eventThrower, @NotNull CameraHandler cameraHandler,
                          @NotNull ControlVectorList controlVectors, @NotNull List<TimedRotation> rotation2dList, boolean reversed,
                          Color color, boolean closed, float velocityStart, float velocityEnd) {
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;

        this.pathRenderer = new PathRenderer(color, controlVectors, rotation2dList.stream().map(TimedRotation::getRotation)
                .collect(Collectors.toList()),
                pathGui.executorService, velocityStart, velocityEnd);
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
                    drawStartY - (35 * 3 + (controlVectors.size() * 30) + 40) - 5, drawWidth - 5,
                    35 * 3 + (controlVectors.size() * 30) + 9, 2);

            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    pathRenderer.getColor(), title, checkWarning(pathGui));

            if (pathRenderer.getSelectionPoint() >= 0) {
                RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5,
                        drawStartY - 42 - (pathRenderer.getSelectionPoint() + 1) * 30, drawWidth - 5, 31, 3, Color.DARK_GRAY);
            }
            
            for (int i = 0; i < textBoxes.size(); i++) {
                List<NumberTextBox> textBoxList = textBoxes.get(i);
                for (int b = 0; b < textBoxList.size(); b++) {
                    textBoxList.get(b).draw(shapeRenderer, spriteBatch, drawStartX + 10 + b * 123, drawStartY - 50 - i * 30,
                            120, null);
                }
            }

            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (63 + controlVectors.size() * 30),
                    Fonts.ROBOTO, 22, new TextComponent("Start Velocity: ").setBold(true).setColor(Color.BLACK));
            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (63 + (controlVectors.size() + 1) * 30),
                    Fonts.ROBOTO, 22, new TextComponent("End Velocity: ").setBold(true).setColor(Color.BLACK));
            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (65 + (controlVectors.size() + 2) * 30),
                    Fonts.ROBOTO, 22, new TextComponent("Reversed: ").setBold(true).setColor(Color.BLACK));

            startVelocityTextBox.draw(shapeRenderer, spriteBatch, drawStartX + 10 + 2 * 123,
                    drawStartY - 50 - controlVectors.size() * 30, 120, null);
            endVelocityTextBox.draw(shapeRenderer, spriteBatch, drawStartX + 10 + 2 * 123,
                    drawStartY - 50 - (controlVectors.size() + 1) * 30, 120, null);

            checkBox.setX(drawStartX + drawWidth - 35);
            checkBox.setY(drawStartY - 43 - (controlVectors.size() + 3) * 30);
            checkBox.checkHover();
            if (checkBox.checkClick()) {
                pathRenderer.setReversed(!pathRenderer.isReversed());
                pathRenderer.updatePath(false);
                UndoHandler.getInstance().somethingChanged();
            }
            checkBox.render(shapeRenderer, spriteBatch, pathRenderer.isReversed());


            return 40 + (controlVectors.size() * 30) + 35 * 3;
        }

    }

    private boolean checkWarning(PathGui pathGui) {
        TrajectoryItem lastTrajectoryItem = pathGui.getLastPath();
        if (lastTrajectoryItem != null) {
            ControlVectorList lastControlVectors = lastTrajectoryItem.getPathRenderer().getControlVectors();
            ControlVector lastPoint = lastControlVectors.get(lastControlVectors.size() - 1);
            return !lastPoint.equals(pathRenderer.getControlVectors().get(0));
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
            NumberTextBox xBox = new NumberTextBox(df.format(controlVector.x[0]), eventThrower, this, i, 0, 18);
            NumberTextBox yBox = new NumberTextBox(df.format(controlVector.y[0]), eventThrower, this, i, 1, 18);
            NumberTextBox rotationBox = new NumberTextBox(df.format(0), eventThrower, this, i, 2,
                    18);
            ArrayList<NumberTextBox> textBoxList = new ArrayList<>();
            textBoxList.add(xBox);
            textBoxList.add(yBox);
            textBoxList.add(rotationBox);
            textBoxes.add(textBoxList);
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
                    pathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], controlVector.x[1], controlVector.x[2]},
                            new double[]{controlVector.y[0], controlVector.y[1], controlVector.y[2]}));
                    //TODO: implement rotation
                    break;
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
