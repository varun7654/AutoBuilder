package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.varun.autobuilder.pathgenerator.PathGenerator;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.util.RoundedShapeRenderer;

public class TrajectoryItem extends GuiItem{
    private PathRenderer pathRenderer;

    public TrajectoryItem(PathRenderer pathRenderer){
        this.pathRenderer = pathRenderer;
    }

    public TrajectoryItem(){

    }

    @Override
    public int render(RoundedShapeRenderer shapeRenderer, int drawStartX, int drawStartY, int drawWidth, Rectangle scissors) {
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.roundedRect(drawStartX, drawStartY - 70, drawWidth, 70, 2 );
        return 70;
    }
}
