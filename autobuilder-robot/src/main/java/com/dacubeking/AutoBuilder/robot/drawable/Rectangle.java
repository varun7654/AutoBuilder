package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Utils;
import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;

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
    public Rectangle(float x, float y, float width, float height, float rotation, Color8Bit color) {
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
    public Rectangle(Vector2 bottomLeftCorner, float width, float height, float rotation, Color8Bit color) {
        super(color);
        this.bottomLeftCorner = bottomLeftCorner;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "R:" + bottomLeftCorner.toString() + "," + width + "," + height + "," + rotation
                + "," + Utils.getColorAsHex(color);
    }
}
