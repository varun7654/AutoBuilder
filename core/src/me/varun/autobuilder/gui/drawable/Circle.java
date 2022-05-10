package me.varun.autobuilder.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import me.varun.autobuilder.AutoBuilder;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A circle that can be drawn to the screen.
 */
public class Circle extends Drawable {
    public final Vector2 center;
    public final float radius;

    public Circle(Vector2 center, float radius, Color color8Bit) {
        super(color8Bit);
        this.center = center;
        this.radius = radius;
    }

    public Circle(float centerX, float centerY, float radius, Color color8Bit) {
        super(color8Bit);
        this.center = new Vector2(centerX, centerY);
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "C:" + center.toString() + "," + radius;
    }

    public static Circle fromString(String s) {
        String[] split = s.split(":");
        split = split[split.length - 1].split(",");
        if (split.length != 3) {
            return null;
        }
        return new Circle(new Vector2().fromString(split[0]), Float.parseFloat(split[1]), Color.valueOf(split[2]));
    }

    @Override
    public void draw(ShapeDrawer drawer, Batch batch) {
        drawer.setColor(color);
        drawer.circle(center.x, center.y, radius, AutoBuilder.LINE_THICKNESS);
    }
}
