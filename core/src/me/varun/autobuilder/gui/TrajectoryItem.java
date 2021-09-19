package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

public class TrajectoryItem extends AbstractGuiItem {
    private @NotNull PathRenderer pathRenderer;

    public TrajectoryItem(@NotNull PathRenderer pathRenderer){
        this.pathRenderer = pathRenderer;
    }

    @Override
    public int render(@NotNull RoundedShapeRenderer shapeRenderer, int drawStartX, int drawStartY, int drawWidth) {
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.roundedRect(drawStartX, drawStartY - 70, drawWidth, 70, 2 );
        return 70;
    }

    public @NotNull PathRenderer getPathRenderer() {
        return pathRenderer;
    }
}
