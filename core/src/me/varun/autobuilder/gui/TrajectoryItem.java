package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.events.pathchange.PathChangeListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.gui.elements.NumberTextBox;
import me.varun.autobuilder.pathing.MovablePointRenderer;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.FileLock;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryItem extends AbstractGuiItem implements PathChangeListener, TextChangeListener {
    private @NotNull PathRenderer pathRenderer;

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;
    private static final @NotNull Color LIGHT_GREY = Color.valueOf("E9E9E9");
    private final @NotNull List<List<NumberTextBox>> textBoxes = new ArrayList<>();
    private final @NotNull InputEventThrower eventThrower;
    private CameraHandler cameraHandler;

    private static final DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(4);
        df.setMinimumIntegerDigits(1);
    }

    public TrajectoryItem(Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower eventThrower, CameraHandler cameraHandler){
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;
        List<Pose2d> pose2dList = new ArrayList<>();
        pose2dList.add(new Pose2d());
        pose2dList.add(new Pose2d(10, 10, Rotation2d.fromDegrees(0)));

        this.fontShader = fontShader;
        this.font = font;

        this.pathRenderer = new PathRenderer(gui.getNextColor(), pose2dList, gui.executorService);
        pathRenderer.setPathChangeListener(this);

    }

    @Override
    public int render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth) {
        List<Pose2d> pose2dList = pathRenderer.getPoint2DList();
        shapeRenderer.setColor(LIGHT_GREY);
        shapeRenderer.roundedRect(drawStartX+5, drawStartY - 40 - pose2dList.size()*30-5, drawWidth-5, (pose2dList.size()*30)+9, 2 );

        shapeRenderer.setColor(pathRenderer.getColor());
        shapeRenderer.roundedRect(drawStartX, drawStartY - 40, drawWidth, 40, 2 );
        shapeRenderer.flush();

        spriteBatch.setShader(fontShader);
        spriteBatch.begin();
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Path", drawStartX+5, drawStartY-5);
        spriteBatch.flush();

        font.getData().setScale(0.4f);
        font.setColor(Color.BLACK);
        for (int i = 0; i < textBoxes.size(); i++) {
            List<NumberTextBox> textBoxList = textBoxes.get(i);
            for (int b = 0; b < textBoxList.size(); b++) {
                textBoxList.get(b).draw(shapeRenderer, spriteBatch, drawStartX + 10 + b*123, drawStartY - 43 - i*30, 120, 28);
            }
        }
        spriteBatch.end();
        spriteBatch.setShader(null);

        return 50 + pose2dList.size()*30;

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

            pathRenderer.updatePath(false);
            cameraHandler.ensureOnScreen(point.getRenderPos3());

        } catch (NumberFormatException ignored){ }

    }
}
