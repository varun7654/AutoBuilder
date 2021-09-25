package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.events.pathchange.PathChangeListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextPositionChangeListener;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.gui.elements.CheckBox;
import me.varun.autobuilder.gui.elements.NumberTextBox;
import me.varun.autobuilder.pathing.MovablePointRenderer;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryItem extends AbstractGuiItem implements PathChangeListener, TextPositionChangeListener {
    private @NotNull PathRenderer pathRenderer;

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;

    private final @NotNull List<List<NumberTextBox>> textBoxes = new ArrayList<>();
    private final @NotNull InputEventThrower eventThrower;
    private final @NotNull CameraHandler cameraHandler;
    private final @NotNull Texture trashTexture;
    private final @NotNull Texture warningTexture;
    private final @NotNull CheckBox checkBox = new CheckBox(0 ,0 , 30, 30);

    private static final DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(4);
        df.setMinimumIntegerDigits(1);
    }



    public TrajectoryItem(Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower eventThrower,
                          @NotNull CameraHandler cameraHandler, @NotNull Texture trashTexture, @NotNull Texture warningTexture){
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;
        this.trashTexture = trashTexture;
        this.warningTexture = warningTexture;
        List<Pose2d> pose2dList = new ArrayList<>();
        pose2dList.add(new Pose2d());
        pose2dList.add(new Pose2d(10, 10, Rotation2d.fromDegrees(0)));

        this.fontShader = fontShader;
        this.font = font;

        this.pathRenderer = new PathRenderer(gui.getNextColor(), pose2dList, gui.executorService);
        pathRenderer.setPathChangeListener(this);

    }


    @Override
    public int render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, Gui gui) {
        super.render(shapeRenderer, spriteBatch, drawStartX, drawStartY, drawWidth, gui);
        if(isClosed()){
            renderHeader(shapeRenderer,spriteBatch, fontShader, font, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture, pathRenderer.getColor(), "Path", checkWarning(gui));
            spriteBatch.end();
            return 40;
        } else {
            List<Pose2d> pose2dList = pathRenderer.getPoint2DList();
            shapeRenderer.setColor(LIGHT_GREY);
            shapeRenderer.roundedRect(drawStartX + 5, drawStartY - (35 + (pose2dList.size() * 30) + 40) - 5, drawWidth - 5, 35 + (pose2dList.size() * 30) + 9, 2);

            renderHeader(shapeRenderer,spriteBatch, fontShader, font, drawStartX, drawStartY, drawWidth, trashTexture, warningTexture, pathRenderer.getColor(), "Path", checkWarning(gui));

            spriteBatch.setShader(fontShader);

            font.setColor(Color.BLACK);
            for (int i = 0; i < textBoxes.size(); i++) {
                List<NumberTextBox> textBoxList = textBoxes.get(i);
                for (int b = 0; b < textBoxList.size(); b++) {
                    textBoxList.get(b).draw(shapeRenderer, spriteBatch, drawStartX + 10 + b * 123, drawStartY - 43 - i * 30, 120, 28);
                }
            }

            spriteBatch.setShader(fontShader);
            font.draw(spriteBatch, "Reversed",drawStartX + 10 + 40, drawStartY - (40 + pose2dList.size() * 30)-10);
            spriteBatch.end();
            spriteBatch.setShader(null);

            checkBox.setX(drawStartX + 10);
            checkBox.setY(drawStartY - (40 + pose2dList.size() * 30) - 35);
            checkBox.checkHover();
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && checkBox.checkClick()){
                pathRenderer.setReversed(!pathRenderer.isReversed());
                pathRenderer.updatePath(false);
            }
            checkBox.render(shapeRenderer, spriteBatch, pathRenderer.isReversed());

            return 40 + (pose2dList.size() * 30) + 35;
        }

    }

    private boolean checkWarning(Gui gui) {
        TrajectoryItem lastTrajectoryItem = gui.getLastPath();
        if(lastTrajectoryItem != null){
            List<Pose2d> lastPose2dList = lastTrajectoryItem.getPathRenderer().getPoint2DList();
            Pose2d lastPoint = lastPose2dList.get(lastPose2dList.size()-1);
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
            Pose2d pose2d =  pathRenderer.getPoint2DList().get(i);
            NumberTextBox xBox = new NumberTextBox(df.format(pose2d.getX()), fontShader, font, eventThrower, this, i, 0);
            NumberTextBox yBox = new NumberTextBox(df.format(pose2d.getY()), fontShader, font, eventThrower, this,  i, 1);
            NumberTextBox rotationBox = new NumberTextBox(df.format(pose2d.getRotation().getDegrees()), fontShader, font, eventThrower,
                    this,  i, 2);
            ArrayList<NumberTextBox> textBoxList = new ArrayList<>();
            textBoxList.add(xBox);
            textBoxList.add(yBox);
            textBoxList.add(rotationBox);
            textBoxes.add(textBoxList);
        }
    }

    @Override
    public void onTextChange(String text, int row, int column) {
        try{
            double parsedNumber = Double.parseDouble(text);

            Pose2d pose2d = pathRenderer.getPoint2DList().get(row);
            MovablePointRenderer point = pathRenderer.getPointList().get(row);
            switch (column){
                case 0:
                    pathRenderer.getPoint2DList().set(row, new Pose2d(parsedNumber, pose2d.getY(), pose2d.getRotation()));
                    point.setX((float) parsedNumber);
                    break;
                case 1:
                    pathRenderer.getPoint2DList().set(row, new Pose2d(pose2d.getX(), parsedNumber, pose2d.getRotation()));
                    point.setY((float) parsedNumber);
                    break;
                case 2:
                    pathRenderer.getPoint2DList().set(row, new Pose2d(pose2d.getX(), pose2d.getY(), Rotation2d.fromDegrees(parsedNumber)));
                    break;
            }

            cameraHandler.ensureOnScreen(point.getRenderPos3());
            pathRenderer.updatePath(false);


        } catch (NumberFormatException ignored){ }

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
