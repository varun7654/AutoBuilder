package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.pathchange.PathChangeListener;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryItem extends AbstractGuiItem implements PathChangeListener {
    private @NotNull PathRenderer pathRenderer;

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;
    static final @NotNull Color LIGHT_GREY = Color.valueOf("E9E9E9");

    private static final DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(4);
        df.setMaximumFractionDigits(4);
    }

    public TrajectoryItem(Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font){
        List<Pose2d> pose2dList = new ArrayList<>();
        pose2dList.add(new Pose2d());
        pose2dList.add(new Pose2d(10, 10, Rotation2d.fromDegrees(0)));

        this.fontShader = fontShader;
        this.font = font;

        this.pathRenderer = new PathRenderer(gui.getNextColor(), pose2dList, gui.executorService, this);

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

        font.getData().setScale(0.4f);
        font.setColor(Color.BLACK);
        for (int i = 0; i < pose2dList.size(); i++) {
            Pose2d point = pose2dList.get(i);
            font.draw(spriteBatch, "(" + df.format(point.getX()) + ", " + df.format(point.getY()) + ")",drawStartX+10, drawStartY - 45 - i*30);
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

    }
}
