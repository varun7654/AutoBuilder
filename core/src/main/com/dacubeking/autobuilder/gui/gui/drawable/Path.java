package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import space.earlygrey.shapedrawer.ShapeDrawer;


/**
 * A path that can be drawn to the screen. (Basically a list of lines.)
 */
public class Path extends Drawable {
    public final Array<Vector2> vertices;

    public Path(Vector2[] vertices, Color color8Bit) {
        super(color8Bit);
        this.vertices = new Array<>(vertices);
    }

    public Path(Color color8Bit, Vector2... vertices) {
        super(color8Bit);
        this.vertices = new Array<>(vertices);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("P:");
        for (Vector2 vertex : vertices) {
            sb.append(vertex.toString()).append(",");
        }
        sb.append(color.toString());
        return sb.toString();
    }

    public static Path fromString(String s) {
        String[] split = s.split(":");
        split = SPLIT_COMMA_NOT_IN_PAREN.split(split[split.length - 1]);
        Vector2[] vertices = new Vector2[split.length - 1];

        for (int i = 0; i < split.length - 1; i++) {
            vertices[i] = new Vector2().fromString(split[i]);
        }

        return new Path(vertices, Color.valueOf(split[split.length - 1]));
    }

    private Array<Vector2> cachedVertices = null;
    private float lastScale = -1;

    @Override
    public void draw(ShapeDrawer drawer, Batch batch) {
        drawer.setColor(color);
        float scale = AutoBuilder.getConfig().getPointScaleFactor();
        if (lastScale != scale) {
            cachedVertices = new Array<>(vertices.size);
            for (int i = 0; i < vertices.size; i++) {
                cachedVertices.add(new Vector2(vertices.get(i)).scl(scale));
            }
            lastScale = scale;
        }
        drawer.path(cachedVertices, AutoBuilder.getLineThickness());
    }
}
