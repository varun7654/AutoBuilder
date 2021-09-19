package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.varun.autobuilder.util.RoundedShapeRenderer;

public abstract class AbstractGuiButton {
    private int x;
    private int y;
    private int width;
    private int height;
    private final Texture texture;
    private boolean hovering;
    private float textureScaleX = 1;
    private float textureScaleY = 1;

    public AbstractGuiButton(int x, int y, int width, int height, Texture texture) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        setWidth(width);
        setHeight(height);
        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);

    }

    public boolean checkClick(Gui gui){
        return hovering;
    }


    public boolean checkHover(){
        return hovering = Gdx.input.getX() >= x && Gdx.input.getX() <= x + width &&
                Gdx.graphics.getHeight() - Gdx.input.getY() >= y && Gdx.graphics.getHeight() - Gdx.input.getY() <= y + height;
    }

    public void render(RoundedShapeRenderer shapeRenderer, SpriteBatch spriteBatch){
        if(hovering){
            shapeRenderer.setColor(Color.LIGHT_GRAY);
        } else {
            shapeRenderer.setColor(Color.WHITE);
        }

        shapeRenderer.roundedRect(x, y, width, height, 4);
        shapeRenderer.flush();
        spriteBatch.begin();
        spriteBatch.draw(texture, x+5, y+5, width-10, height-10);
        spriteBatch.end();

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
}
