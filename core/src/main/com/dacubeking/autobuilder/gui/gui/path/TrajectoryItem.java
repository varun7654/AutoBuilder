package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.CameraHandler;
import com.dacubeking.autobuilder.gui.events.input.NumberTextboxChangeListener;
import com.dacubeking.autobuilder.gui.events.pathchange.PathChangeListener;
import com.dacubeking.autobuilder.gui.gui.elements.CheckBox;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.PositionedNumberTextBox;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.pathing.MovablePointRenderer;
import com.dacubeking.autobuilder.gui.pathing.TimedRotation;
import com.dacubeking.autobuilder.gui.pathing.TrajectoryPathRenderer;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Rotation2d;
import com.dacubeking.autobuilder.gui.wpi.math.spline.Spline;
import com.dacubeking.autobuilder.gui.wpi.math.spline.Spline.ControlVector;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.dacubeking.autobuilder.gui.util.Colors.LIGHT_GREY;

public class TrajectoryItem extends AbstractGuiItem implements PathChangeListener, NumberTextboxChangeListener {
    public static final TextBlock END_VELOCITY_LABEL = new TextBlock(Fonts.ROBOTO, 22,
            new TextComponent("End Velocity:").setBold(true).setColor(Color.BLACK));
    public static final TextBlock START_VELOCITY_LABEL = new TextBlock(Fonts.ROBOTO, 22,
            new TextComponent("Start Velocity:").setBold(true).setColor(Color.BLACK));
    public static final TextBlock REVERSE_LABEL = new TextBlock(Fonts.ROBOTO, 22, new TextComponent("Reverse:").setBold(
            true).setColor(
            Color.BLACK));
    private static final DecimalFormat df = new DecimalFormat();
    public static final int TEXTBOX_WIDTH = 120;
    public static final float TEXTBOX_X_PADDING = 3;
    private static final float TEXTBOX_Y_PADDING = 3;
    private static final float TEXTBOX_Y_PADDING_NEW_POINT = 3;

    static {
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(4);
        df.setMinimumIntegerDigits(1);
    }

    private @NotNull final TrajectoryPathRenderer trajectoryPathRenderer;
    private final @NotNull List<List<NumberTextBox>> textBoxes = new ArrayList<>();
    private final @NotNull CameraHandler cameraHandler;
    private final @NotNull CheckBox reversedCheckBox = new CheckBox(0, 0, 30, 30);
    private final @NotNull NumberTextBox startVelocityTextBox;
    private final @NotNull NumberTextBox endVelocityTextBox;

    private static final TextComponent warningText =
            new TextComponent("This path does not begin at the end of the previous path");


    public TrajectoryItem(@NotNull PathGui pathGui, @NotNull CameraHandler cameraHandler) {
        this.cameraHandler = cameraHandler;
        ControlVectorList controlVectorList = new ControlVectorList();
        controlVectorList.add(new ControlVector(new double[]{0, 2, 0}, new double[]{0, 0, 0}));
        controlVectorList.add(new ControlVector(new double[]{2, 2, 0}, new double[]{2, 0, 0}));
        List<Rotation2d> rotation2dList = new ArrayList<>();
        rotation2dList.add(Rotation2d.fromDegrees(0));
        rotation2dList.add(Rotation2d.fromDegrees(0));

        this.trajectoryPathRenderer = new TrajectoryPathRenderer(pathGui.getNextColor(), controlVectorList, rotation2dList,
                pathGui.executorService, 0, 0, new ArrayList<>());
        trajectoryPathRenderer.setPathChangeListener(this);

        startVelocityTextBox = new PositionedNumberTextBox(df.format(getPathRenderer().getVelocityStart()), this, 0, 0, 18);
        endVelocityTextBox = new PositionedNumberTextBox(df.format(getPathRenderer().getVelocityEnd()), this, 0, 0, 18);
        setInitialClosed(false);
    }

    public TrajectoryItem(PathGui pathGui, @NotNull CameraHandler cameraHandler,
                          @NotNull ControlVectorList controlVectors, @NotNull List<TimedRotation> rotation2dList,
                          boolean reversed,
                          Color color, boolean closed, float velocityStart, float velocityEnd,
                          @NotNull List<TrajectoryConstraint> constraints) {
        this.cameraHandler = cameraHandler;

        this.trajectoryPathRenderer = new TrajectoryPathRenderer(color, controlVectors,
                rotation2dList.stream().map(TimedRotation::getRotation).collect(Collectors.toList()),
                pathGui.executorService, velocityStart, velocityEnd, constraints);

        if (AutoBuilder.getConfig().isHolonomic()) {
            trajectoryPathRenderer.setReversed(false);
        } else {
            trajectoryPathRenderer.setReversed(reversed);
        }

        trajectoryPathRenderer.setPathChangeListener(this);
        startVelocityTextBox = new PositionedNumberTextBox(df.format(getPathRenderer().getVelocityStart()), this, 0, 0, 18);
        endVelocityTextBox = new PositionedNumberTextBox(df.format(getPathRenderer().getVelocityEnd()), this, 0, 0, 18);
        setInitialClosed(closed);
    }

    private static final TextBlock X_TEXT = new TextBlock(Fonts.ROBOTO, 13, new TextComponent("X (meters)"));
    private static final TextBlock Y_TEXT = new TextBlock(Fonts.ROBOTO, 13, new TextComponent("Y (meters)"));
    private static final TextBlock THETA_TEXT = new TextBlock(Fonts.ROBOTO, 13, new TextComponent("Theta (°)"));
    private static final TextBlock CONTROL_POINT_X = new TextBlock(Fonts.ROBOTO, 13, new TextComponent("X Control Point"));
    private static final TextBlock CONTROL_POINT_Y = new TextBlock(Fonts.ROBOTO, 13, new TextComponent("Y Control Point"));

    private static final TextBlock[] HOVER_TEXT = {X_TEXT, Y_TEXT, THETA_TEXT, CONTROL_POINT_X, CONTROL_POINT_Y};

    @Override
    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX, int drawStartY,
                      int drawWidth, PathGui pathGui, Camera camera, boolean isLeftMouseJustUnpressed) {
        int pop = super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, pathGui, camera,
                isLeftMouseJustUnpressed);
        String title;
        if (trajectoryPathRenderer.getTrajectory() != null) {
            title = "Path - " + df.format(trajectoryPathRenderer.getTrajectory().getTotalTimeSeconds()) + "s";
        } else {
            title = "Path - Calculating";
        }
        if (isFullyClosed()) {
            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    trajectoryPathRenderer.getColor(), title, checkWarning(pathGui), warningText);
        } else {
            shapeRenderer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5,
                    drawStartY - getHeight(),
                    drawWidth - 5,
                    getHeight(), 2);

            renderHeader(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture,
                    trajectoryPathRenderer.getColor(), title, checkWarning(pathGui), warningText);

            float textBoxDrawX = drawStartX + 10;
            float textBoxDrawY = drawStartY - 50;
            // Draw all the control vectors
            for (int i = 0; i < textBoxes.size(); i++) {
                List<NumberTextBox> textBoxList = textBoxes.get(i);
                float maxTextBoxHeight = 28;

                if (i == trajectoryPathRenderer.getSelectionPoint()) {
                    float height = getHeightOfTextBoxRow(textBoxList, drawWidth) + TEXTBOX_Y_PADDING_NEW_POINT;

                    shapeRenderer.setColor(Color.DARK_GRAY);
                    RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 5, textBoxDrawY - height + 11,
                            drawWidth - 5, height, 3);
                    shapeRenderer.setColor(Color.WHITE);
                }

                for (int j = 0; j < textBoxList.size(); j++) {
                    if (textBoxList.get(j).draw(shapeRenderer, spriteBatch, textBoxDrawX, textBoxDrawY, TEXTBOX_WIDTH, null)) {
                        HoverManager.setHoverText(HOVER_TEXT[j], textBoxDrawX + (TEXTBOX_WIDTH / 2f), textBoxDrawY + 10);
                    }
                    maxTextBoxHeight = Math.max(textBoxList.get(j).getHeight(), maxTextBoxHeight);
                    textBoxDrawX += TEXTBOX_WIDTH + TEXTBOX_X_PADDING;
                    if (textBoxDrawX > drawStartX + drawWidth - TEXTBOX_WIDTH - TEXTBOX_X_PADDING || j == textBoxList.size() - 1) {
                        textBoxDrawX = drawStartX + 10;
                        textBoxDrawY -= maxTextBoxHeight + TEXTBOX_Y_PADDING;
                        maxTextBoxHeight = 28;
                    }
                }
                textBoxDrawY -= TEXTBOX_Y_PADDING_NEW_POINT;
            }

            if (START_VELOCITY_LABEL.getWidth() + TEXTBOX_WIDTH + TEXTBOX_X_PADDING * 4 < drawWidth) {
                FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, textBoxDrawY - 13, START_VELOCITY_LABEL);
            }
            startVelocityTextBox.draw(shapeRenderer, spriteBatch, drawStartX + drawWidth - TEXTBOX_WIDTH - TEXTBOX_X_PADDING,
                    textBoxDrawY, TEXTBOX_WIDTH, null);


            textBoxDrawY -= startVelocityTextBox.getHeight() + TEXTBOX_Y_PADDING_NEW_POINT;
            if (END_VELOCITY_LABEL.getWidth() + TEXTBOX_WIDTH + TEXTBOX_X_PADDING * 4 < drawWidth) {
                FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, textBoxDrawY - 13, END_VELOCITY_LABEL);
            }
            endVelocityTextBox.draw(shapeRenderer, spriteBatch, drawStartX + drawWidth - TEXTBOX_WIDTH - TEXTBOX_X_PADDING,
                    textBoxDrawY, TEXTBOX_WIDTH, null);

            if (!AutoBuilder.getConfig().isHolonomic()) { //Don't allow reversing the path if holonomic
                textBoxDrawY -= endVelocityTextBox.getHeight() + TEXTBOX_Y_PADDING_NEW_POINT;
                if (REVERSE_LABEL.getWidth() + reversedCheckBox.getWidth() + TEXTBOX_X_PADDING * 4 < drawWidth) {
                    FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, textBoxDrawY - 13, REVERSE_LABEL);
                }

                reversedCheckBox.setPosition(drawStartX + drawWidth - reversedCheckBox.getWidth() - TEXTBOX_X_PADDING,
                        textBoxDrawY - 22);
                reversedCheckBox.checkHover();
                if (reversedCheckBox.checkClick()) {
                    trajectoryPathRenderer.setReversed(!trajectoryPathRenderer.isReversed());
                    trajectoryPathRenderer.updatePath(false);
                    UndoHandler.getInstance().somethingChanged();
                }
                reversedCheckBox.render(shapeRenderer, spriteBatch, trajectoryPathRenderer.isReversed());
            }
        }
        spriteBatch.flush();
        if (pop == 1) ScissorStack.popScissors();
        return getHeight();
    }

    private static final double allowedAngleError = 1e-2;

    private boolean checkWarning(PathGui pathGui) {
        TrajectoryItem lastTrajectoryItem = pathGui.getLastPath();
        if (lastTrajectoryItem != null) {
            List<Spline.ControlVector> lastControlVectors = lastTrajectoryItem.getPathRenderer().getControlVectors();
            ControlVector lastPoint = lastControlVectors.get(lastControlVectors.size() - 1);
            ControlVector nextPoint = trajectoryPathRenderer.getControlVectors().get(0);
            if (AutoBuilder.getConfig().isHolonomic()) {
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

    public @NotNull TrajectoryPathRenderer getPathRenderer() {
        return trajectoryPathRenderer;
    }

    @Override
    public void onPathChange() {
        for (List<NumberTextBox> textBox : textBoxes) {
            for (NumberTextBox box : textBox) {
                box.dispose();
            }
        }
        textBoxes.clear();
        for (int i = 0; i < trajectoryPathRenderer.getControlVectors().size(); i++) {
            ControlVector controlVector = trajectoryPathRenderer.getControlVectors().get(i);
            Rotation2d rotation = trajectoryPathRenderer.getRotations().get(i);
            NumberTextBox xBox = new PositionedNumberTextBox(df.format(controlVector.x[0]), this, i, 0, 18);
            NumberTextBox yBox = new PositionedNumberTextBox(df.format(controlVector.y[0]), this, i, 1, 18);
            double rotationDegrees = AutoBuilder.getConfig().isHolonomic() ?
                    rotation.getDegrees() : Math.toDegrees(Math.atan2(controlVector.y[1], controlVector.x[1]));
            NumberTextBox rotationBox = new PositionedNumberTextBox(df.format(rotationDegrees), this, i, 2, 18);

            NumberTextBox xControlBox = new PositionedNumberTextBox(df.format(controlVector.x[1]), this, i, 3, 18);
            NumberTextBox yControlBox = new PositionedNumberTextBox(df.format(controlVector.y[1]), this, i, 4, 18);

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
                trajectoryPathRenderer.updatePath(false);
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        if (numberTextBox == endVelocityTextBox) {
            try {
                float parsedNumber = Float.parseFloat(text);
                getPathRenderer().setVelocityEnd(parsedNumber);
                trajectoryPathRenderer.updatePath(false);
            } catch (NumberFormatException ignored) {
            }
            return;
        }


        try {
            double parsedNumber = Double.parseDouble(text);

            ControlVector controlVector = trajectoryPathRenderer.getControlVectors().get(row);
            MovablePointRenderer point = trajectoryPathRenderer.getPointList().get(row);
            switch (column) {
                case 0:
                    trajectoryPathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{parsedNumber, controlVector.x[1], controlVector.x[2]},
                            new double[]{controlVector.y[0], controlVector.y[1], controlVector.y[2]}));
                    point.setX((float) parsedNumber);
                    break;
                case 1:
                    trajectoryPathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], controlVector.x[1], controlVector.x[2]},
                            new double[]{parsedNumber, controlVector.y[1], controlVector.y[2]}));
                    point.setY((float) parsedNumber);
                    break;
                case 2:
                    if (AutoBuilder.getConfig().isHolonomic()) {
                        trajectoryPathRenderer.getRotations().set(row, Rotation2d.fromDegrees(parsedNumber));
                    } else {
                        Vector2 v = new Vector2((float) controlVector.y[1], (float) controlVector.x[1]);
                        v.setAngleDeg((float) parsedNumber);
                        trajectoryPathRenderer.getControlVectors().set(row, new ControlVector(
                                new double[]{controlVector.x[0], v.x, controlVector.x[2]},
                                new double[]{controlVector.y[0], v.y, controlVector.y[2]}));
                    }
                    break;
                case 3:
                    trajectoryPathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], parsedNumber, controlVector.x[2]},
                            new double[]{controlVector.y[0], controlVector.y[1], controlVector.y[2]}));
                    break;
                case 4:
                    trajectoryPathRenderer.getControlVectors().set(row, new ControlVector(
                            new double[]{controlVector.x[0], controlVector.x[1], controlVector.x[2]},
                            new double[]{controlVector.y[0], parsedNumber, controlVector.y[2]}));
            }

            cameraHandler.ensureOnScreen(point.getRenderPos3());
            trajectoryPathRenderer.updatePath(false);
            trajectoryPathRenderer.updatePointHandles();
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public String onTextBoxClick(String text, int row, int column, NumberTextBox numberTextBox) {
        if (numberTextBox == startVelocityTextBox || numberTextBox == endVelocityTextBox) {
            return text;
        }

        cameraHandler.ensureOnScreen(trajectoryPathRenderer.getPointList().get(row).getRenderPos3());
        return text;
    }

    @Override
    public void dispose() {
        super.dispose();
        for (List<NumberTextBox> textBox : textBoxes) {
            for (NumberTextBox box : textBox) {
                box.dispose();
            }
        }
    }

    @Override
    public int getOpenHeight(float drawWidth) {
        float textBoxDrawX = 10;
        float textBoxDrawY = -35;
        // Draw all the control vectors
        for (List<NumberTextBox> textBoxList : textBoxes) {
            for (int i = 0; i < textBoxList.size(); i++) {
                float maxTextBoxHeight = 28;
                maxTextBoxHeight = Math.max(textBoxList.get(i).getHeight(), maxTextBoxHeight);
                textBoxDrawX += TEXTBOX_WIDTH + TEXTBOX_X_PADDING;
                if (textBoxDrawX > drawWidth - TEXTBOX_WIDTH - TEXTBOX_X_PADDING || i == textBoxList.size() - 1) {
                    textBoxDrawX = 10;
                    textBoxDrawY -= maxTextBoxHeight + TEXTBOX_Y_PADDING;
                }
            }
            textBoxDrawY -= TEXTBOX_Y_PADDING_NEW_POINT;
        }

        textBoxDrawY -= startVelocityTextBox.getHeight() + TEXTBOX_Y_PADDING_NEW_POINT;
        if (!AutoBuilder.getConfig().isHolonomic()) { //Don't allow reversing the path if holonomic
            textBoxDrawY -= endVelocityTextBox.getHeight() + TEXTBOX_Y_PADDING_NEW_POINT;
        }

        return (int) -textBoxDrawY;
    }

    private float getHeightOfTextBoxRow(@NotNull List<NumberTextBox> textBoxList, float drawWidth) {
        float textBoxDrawX = 10;
        float textBoxDrawY = 0;

        for (int i = 0; i < textBoxList.size(); i++) {
            float maxTextBoxHeight = 28;
            maxTextBoxHeight = Math.max(textBoxList.get(i).getHeight(), maxTextBoxHeight);
            textBoxDrawX += TEXTBOX_WIDTH + TEXTBOX_X_PADDING;
            if (textBoxDrawX > drawWidth - TEXTBOX_WIDTH - TEXTBOX_X_PADDING || i == textBoxList.size() - 1) {
                textBoxDrawX = 10;
                textBoxDrawY += maxTextBoxHeight + TEXTBOX_Y_PADDING;
            }
        }
        return textBoxDrawY;
    }

    @Override
    public String toString() {
        return "TrajectoryItem{" +
                "pathRenderer=" + trajectoryPathRenderer +
                '}';
    }
}
