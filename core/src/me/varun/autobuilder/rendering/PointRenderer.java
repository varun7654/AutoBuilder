package me.varun.autobuilder.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.util.MathUntil;

import static java.lang.Math.tan;
import static me.varun.autobuilder.util.MathUntil.clamp;

public class PointRenderer {
    protected float x;
    protected float y;
    protected Color color;
    protected final float radius;

    public PointRenderer(float x, float y, Color color, float radius) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
    }

    public PointRenderer(Vector2 pos, Color color, float radius) {
        this.x = pos.x;
        this.y = pos.y;
        this.color = color;
        this.radius = radius;
    }

    public PointRenderer(Vector3 pos, Color color, float radius) {
        this.x = pos.x;
        this.y = pos.y;
        this.color = color;
        this.radius = radius;
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(ShapeRenderer shape, OrthographicCamera camera) {
        shape.setColor(color);
        shape.circle(x* AutoBuilder.POINT_SCALE_FACTOR, y*AutoBuilder.POINT_SCALE_FACTOR, radius, (int) (MathUntil.clamp(20/tan(camera.zoom), 5, 50)));
    }


    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setPosition(Vector2 position){
        setX(position.x);
        setY(position.y);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Vector2 getPos2(){
        return new Vector2(x,y);
    }

    public Vector3 getPos3(){
        return new Vector3(x,y,0);
    }

    public Vector3 getRenderPos3(){
        return new Vector3(x*AutoBuilder.POINT_SCALE_FACTOR, y*AutoBuilder.POINT_SCALE_FACTOR ,0);
    }

    @Override
    public String toString() {
        return "PointRenderer{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}
