package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A rectangle that can be drawn to the screen.
 */
public class Rectangle extends Drawable {
    public final Vector2 bottomLeftCorner;

    public float width;
    public float height;
    public float rotation;


    /**
     * @param x        the x-coordinate of the bottom left corner of the rectangle
     * @param y        the y-coordinate of the bottom left corner of the rectangle
     * @param width    the width of the rectangle
     * @param height   the height of the rectangle
     * @param rotation the anticlockwise rotation in radians
     */
    public Rectangle(float x, float y, float width, float height, float rotation, Color color) {
        super(color);
        this.bottomLeftCorner = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    /**
     * @param bottomLeftCorner the bottom left corner of the rectangle
     * @param width            the width of the rectangle
     * @param height           the height of the rectangle
     * @param rotation         the anticlockwise rotation in radians
     * @param color            the color of the rectangle
     */
    public Rectangle(Vector2 bottomLeftCorner, float width, float height, float rotation, Color color) {
        super(color);
        this.bottomLeftCorner = bottomLeftCorner;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "R:" + bottomLeftCorner.toString() + "," + width + "," + height + "," + rotation
                + "," + color.toString();
    }

    public static @Nullable Rectangle fromString(String s) {
        String[] split = s.split(":");
        split = SPLIT_COMMA_NOT_IN_PAREN.split(split[split.length - 1]);
        if (split.length != 5) {
            return null;
        }
        return new Rectangle(new Vector2().fromString(split[0]), Float.parseFloat(split[1]),
                Float.parseFloat(split[2]), Float.parseFloat(split[3]), Color.valueOf(split[4]));
    }

    @Override
    public void draw(ShapeDrawer drawer, Batch batch) {
        drawer.rectangle(bottomLeftCorner.x, bottomLeftCorner.y, width, height, AutoBuilder.LINE_THICKNESS, rotation);
    }
}
