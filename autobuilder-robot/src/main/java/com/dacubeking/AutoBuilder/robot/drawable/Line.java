package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Utils;
import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;

/**
 * A line that can be drawn to the screen.
 */
public class Line extends Drawable {
    public final Vector2 start;
    public final Vector2 end;

    public Line(Vector2 start, Vector2 end, Color8Bit color8Bit) {
        super(color8Bit);
        this.start = start;
        this.end = end;
    }

    public Line(float startX, float startY, float endX, float endY, Color8Bit color8Bit) {
        super(color8Bit);
        this.start = new Vector2(startX, startY);
        this.end = new Vector2(endX, endY);
    }

    @Override
    public String toString() {
        return "L:" + start.toString() + "," + end.toString() + "," + Utils.getColorAsHex(color);
    }
}
