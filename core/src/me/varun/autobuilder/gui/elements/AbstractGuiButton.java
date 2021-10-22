package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import me.varun.autobuilder.gui.path.PathGui;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class AbstractGuiButton implements Disposable {
    private final @NotNull Texture texture;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean hovering;

    public AbstractGuiButton(int x, int y, int width, int height, @NotNull Texture texture) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        setWidth(width);
        setHeight(height);
        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);

    }

    public boolean checkClick(PathGui pathGui) {
        return this.hovering;
    }

    public boolean checkClick() {
        return hovering;
    }


    public boolean checkHover() {
        return hovering = Gdx.input.getX() >= x && Gdx.input.getX() <= x + width &&
                Gdx.graphics.getHeight() - Gdx.input.getY() >= y && Gdx.graphics.getHeight() - Gdx.input.getY() <= y + height;
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void dispose() {
        texture.dispose();
    }
}
