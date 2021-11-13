package me.varun.autobuilder.pathing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.Config;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PointRenderer {
    protected final float radius;
    protected float x;
    protected float y;
    protected Color color;

    Config config = AutoBuilder.getConfig();

    public PointRenderer(float x, float y, Color color, float radius) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
    }

    public PointRenderer(@NotNull Vector2 pos, Color color, float radius) {
        this.x = pos.x;
        this.y = pos.y;
        this.color = color;
        this.radius = radius;
    }

    public PointRenderer(@NotNull Vector3 pos, Color color, float radius) {
        this.x = pos.x;
        this.y = pos.y;
        this.color = color;
        this.radius = radius;
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(@NotNull ShapeDrawer shape, @NotNull OrthographicCamera camera) {
        shape.filledCircle(x * config.getPointScaleFactor(), y * config.getPointScaleFactor(), radius, color);
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

    public void setPosition(@NotNull Vector2 position) {
        setX(position.x);
        setY(position.y);
    }

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public @NotNull Vector2 getPos2() {
        return new Vector2(x, y);
    }

    public @NotNull Vector3 getPos3() {
        return new Vector3(x, y, 0);
    }

    public @NotNull Vector3 getRenderPos3() {
        return new Vector3(x * config.getPointScaleFactor(), y * config.getPointScaleFactor(), 0);
    }

    public @NotNull Vector2 getRenderPos2() {
        return new Vector2(x * config.getPointScaleFactor(), y * config.getPointScaleFactor());
    }

    @Override
    public @NotNull String toString() {
        return "PointRenderer{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}
