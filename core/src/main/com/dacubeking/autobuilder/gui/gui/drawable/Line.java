package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A line that can be drawn to the screen.
 */
public class Line extends Drawable {
    public final Vector2 start;
    public final Vector2 end;

    public Line(Vector2 start, Vector2 end, Color color8Bit) {
        super(color8Bit);
        this.start = start;
        this.end = end;
    }

    public Line(float startX, float startY, float endX, float endY, Color color8Bit) {
        super(color8Bit);
        this.start = new Vector2(startX, startY);
        this.end = new Vector2(endX, endY);
    }

    @Override
    public String toString() {
        return "L:" + start.toString() + "," + end.toString();
    }

    @Override
    public void draw(ShapeDrawer drawer, Batch batch) {
        drawer.setColor(color);
        drawer.line(start.x * AutoBuilder.getConfig().getPointScaleFactor(),
                start.y * AutoBuilder.getConfig().getPointScaleFactor(),
                end.x * AutoBuilder.getConfig().getPointScaleFactor(),
                end.y * AutoBuilder.getConfig().getPointScaleFactor(), AutoBuilder.LINE_THICKNESS);
    }

    public static @Nullable Line fromString(String line) {
        String[] split = line.split(":");
        split = SPLIT_COMMA_NOT_IN_PAREN.split(split[split.length - 1]);
        if (split.length != 3) {
            return null;
        }
        return new Line(new Vector2().fromString(split[0]), new Vector2().fromString(split[1]), Color.valueOf(split[2]));
    }
}
