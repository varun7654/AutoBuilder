package me.varun.autobuilder.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import me.varun.autobuilder.AutoBuilder;
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
        drawer.line(start.x, start.y, end.x, end.y, AutoBuilder.LINE_THICKNESS);
    }

    public static @Nullable Line fromString(String line) {
        String[] split = line.split(":");
        split = split[split.length - 1].split(",");
        if (split.length != 4) {
            return null;
        }
        return new Line(new Vector2().fromString(split[1]), new Vector2().fromString(split[2]), Color.valueOf(split[3]));
    }
}
