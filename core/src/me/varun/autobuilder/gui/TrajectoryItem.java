package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

public class TrajectoryItem extends AbstractGuiItem {
    private @NotNull PathRenderer pathRenderer;

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;

    private static final DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(4);
        df.setMaximumFractionDigits(4);
    }

    public TrajectoryItem(@NotNull PathRenderer pathRenderer, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font){
        this.pathRenderer = pathRenderer;
        this.fontShader = fontShader;
        this.font = font;
    }

    @Override
    public int render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth) {
        List<Pose2d> pose2dList = pathRenderer.getPoint2DList();
        shapeRenderer.setColor(Color.valueOf("E9E9E9"));
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
}
