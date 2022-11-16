package com.dacubeking.autobuilder.gui.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseX;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseY;

public abstract class AbstractGuiButton implements Disposable {
    private final @NotNull Texture texture;
    private float x;
    private float y;
    private float width;
    private float height;
    private boolean hovering;

    public AbstractGuiButton(float x, float y, float width, float height, @NotNull Texture texture) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        setWidth(width);
        setHeight(height);
        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
    }

    public AbstractGuiButton(@NotNull Texture texture) {
        this(0, 0, 0, 0, texture);
    }

    public boolean checkClick() {
        if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
            return hovering;
        }
        return false;
    }

    public boolean checkRightClick() {
        if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
            return hovering;
        }
        return false;
    }


    public boolean checkHover() {
        return hovering = getMouseX() >= x && getMouseX() <= x + width && getMouseY() >= y && getMouseY() <= y + height;
    }

    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull Batch spriteBatch) {
        if (hovering) {
            shapeRenderer.setColor(Color.LIGHT_GRAY);
        } else {
            shapeRenderer.setColor(Color.WHITE);
        }

        RoundedShapeRenderer.roundedRect(shapeRenderer, x, y, width, height, 4);
        spriteBatch.draw(texture, x + 5, y + 5, width - 10, height - 10);
    }

    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull Batch spriteBatch, boolean renderTexture) {
        if (hovering) {
            shapeRenderer.setColor(Color.LIGHT_GRAY);
        } else {
            shapeRenderer.setColor(Color.WHITE);
        }
        RoundedShapeRenderer.roundedRect(shapeRenderer, x, y, width, height, 4);
        if (renderTexture) {
            spriteBatch.draw(texture, x + 5, y + 5, width - 10, ((width - 10f) / (texture.getWidth())) * texture.getHeight());
        }
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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getRightX() {
        return getX() + getWidth();
    }

    public float getTopY() {
        return getY() + getHeight();
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
