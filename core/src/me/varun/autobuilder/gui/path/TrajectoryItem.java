package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.pathchange.PathChangeListener;
import me.varun.autobuilder.events.input.InputEventThrower;
import me.varun.autobuilder.events.input.NumberTextboxChangeListener;
import me.varun.autobuilder.gui.elements.CheckBox;
import me.varun.autobuilder.gui.elements.NumberTextBox;
import me.varun.autobuilder.gui.textrendering.FontRenderer;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.pathing.MovablePointRenderer;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
        List<Pose2d> pose2dList = new ArrayList<>();
        pose2dList.add(new Pose2d());
        pose2dList.add(new Pose2d(2, 2, Rotation2d.fromDegrees(0)));

        this.pathRenderer = new PathRenderer(pathGui.getNextColor(), pose2dList, pathGui.executorService, 0, 0);
        pathRenderer.setPathChangeListener(this);

        startVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityStart()), eventThrower, this, 0, 0, 18);
        endVelocityTextBox = new NumberTextBox(df.format(getPathRenderer().getVelocityEnd()), eventThrower, this, 0, 0, 18);
    }

    public TrajectoryItem(PathGui pathGui, @NotNull InputEventThrower eventThrower, @NotNull CameraHandler cameraHandler,
                          List<Pose2d> pose2dList, boolean reversed, Color color, boolean closed, float velocityStart,
                          float velocityEnd) {
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;

        this.pathRenderer = new PathRenderer(color, pose2dList, pathGui.executorService, velocityStart, velocityEnd);
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
        } else title = "Path - Calculating Time";
        if (isClosed()) {
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    pathRenderer.getColor(), title, checkWarning(pathGui));
            return 40;
        } else {
            List<Pose2d> pose2dList = pathRenderer.getPoint2DList();
            shapeRenderer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5,
                    drawStartY - (35 * 3 + (pose2dList.size() * 30) + 40) - 5, drawWidth - 5,
                    35 * 3 + (pose2dList.size() * 30) + 9, 2);

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
                            120);
                }
            }

            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (63 + pose2dList.size() * 30),
                    Fonts.ROBOTO, 22, new TextComponent("Start Velocity: ").setBold(true).setColor(Color.BLACK));
            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (63 + (pose2dList.size() + 1) * 30),
                    Fonts.ROBOTO, 22, new TextComponent("End Velocity: ").setBold(true).setColor(Color.BLACK));
            FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - (65 + (pose2dList.size() + 2) * 30),
                    Fonts.ROBOTO, 22, new TextComponent("Reversed: ").setBold(true).setColor(Color.BLACK));

            startVelocityTextBox.draw(shapeRenderer, spriteBatch, drawStartX + 10 + 2 * 123,
                    drawStartY - 50 - pose2dList.size() * 30, 120);
            endVelocityTextBox.draw(shapeRenderer, spriteBatch, drawStartX + 10 + 2 * 123,
                    drawStartY - 50 - (pose2dList.size() + 1) * 30, 120);

            checkBox.setX(drawStartX + drawWidth - 35);
            checkBox.setY(drawStartY - 43 - (pose2dList.size() + 3) * 30);
            checkBox.checkHover();
            if (checkBox.checkClick()) {
                pathRenderer.setReversed(!pathRenderer.isReversed());
                pathRenderer.updatePath(false);
                UndoHandler.getInstance().somethingChanged();
            }
            checkBox.render(shapeRenderer, spriteBatch, pathRenderer.isReversed());


            return 40 + (pose2dList.size() * 30) + 35 * 3;
        }

    }

    private boolean checkWarning(PathGui pathGui) {
        TrajectoryItem lastTrajectoryItem = pathGui.getLastPath();
        if (lastTrajectoryItem != null) {
            List<Pose2d> lastPose2dList = lastTrajectoryItem.getPathRenderer().getPoint2DList();
            Pose2d lastPoint = lastPose2dList.get(lastPose2dList.size() - 1);
            return !lastPoint.equals(pathRenderer.getPoint2DList().get(0));
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
        for (int i = 0; i < pathRenderer.getPoint2DList().size(); i++) {
            Pose2d pose2d = pathRenderer.getPoint2DList().get(i);
            NumberTextBox xBox = new NumberTextBox(df.format(pose2d.getX()), eventThrower, this, i, 0, 18);
            NumberTextBox yBox = new NumberTextBox(df.format(pose2d.getY()), eventThrower, this, i, 1, 18);
            NumberTextBox rotationBox = new NumberTextBox(df.format(pose2d.getRotation().getDegrees()), eventThrower, this, i, 2,
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

            Pose2d pose2d = pathRenderer.getPoint2DList().get(row);
            MovablePointRenderer point = pathRenderer.getPointList().get(row);
            switch (column) {
                case 0:
                    pathRenderer.getPoint2DList().set(row, new Pose2d(parsedNumber, pose2d.getY(), pose2d.getRotation()));
                    point.setX((float) parsedNumber);
                    break;
                case 1:
                    pathRenderer.getPoint2DList().set(row, new Pose2d(pose2d.getX(), parsedNumber, pose2d.getRotation()));
                    point.setY((float) parsedNumber);
                    break;
                case 2:
                    pathRenderer.getPoint2DList()
                            .set(row, new Pose2d(pose2d.getX(), pose2d.getY(), Rotation2d.fromDegrees(parsedNumber)));
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
