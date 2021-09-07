package me.varun.autobuilder.rendering;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class Point {
    float x;
    float y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Vector2 pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(ShapeRenderer shape) {
        shape.circle(x, y, 5);
    }
}
