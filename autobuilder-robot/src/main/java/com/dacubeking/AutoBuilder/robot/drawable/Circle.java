package com.dacubeking.AutoBuilder.robot.drawable;


import com.dacubeking.AutoBuilder.robot.utility.Utils;
import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;

/**
 * A circle that can be drawn to the screen.
 */
public class Circle extends Drawable {
    public final Vector2 center;
    public final float radius;

    public Circle(Vector2 center, float radius, Color8Bit color8Bit) {
        super(color8Bit);
        this.center = center;
        this.radius = radius;
    }

    public Circle(float centerX, float centerY, float radius, Color8Bit color8Bit) {
        super(color8Bit);
        this.center = new Vector2(centerX, centerY);
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "C:" + center.toString() + "," + radius + "," + Utils.getColorAsHex(color);
    }
}
