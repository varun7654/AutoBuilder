package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Utils;
import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;

/**
 * A path that can be drawn to the screen. (Basically a list of lines.)
 */
public class Path extends Drawable {
    public final Vector2[] vertices;

    public Path(Vector2[] vertices, Color8Bit color8Bit) {
        super(color8Bit);
        this.vertices = vertices;
    }

    public Path(Color8Bit color8Bit, Vector2... vertices) {
        super(color8Bit);
        this.vertices = vertices;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("P:");
        for (Vector2 vertex : vertices) {
            sb.append(vertex.toString()).append(",");
        }
        sb.append(Utils.getColorAsHex(color));
        return sb.toString();
    }
}
