package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import me.varun.autobuilder.gui.Gui;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGuiItem implements Disposable {

    protected static final @NotNull Color LIGHT_GREY = Color.valueOf("E9E9E9");

    private boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    abstract public void dispose();

    static protected final Texture trashTexture;
    static protected final Texture warningTexture;
    static {
        warningTexture = new Texture(Gdx.files.internal("warning.png"), true);
        trashTexture = new Texture(Gdx.files.internal("trash.png"), true);
        warningTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
        trashTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
    }

    public int render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, Gui gui){
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            if(isMouseOver(drawStartX + drawWidth -45, drawStartY-40, drawWidth -5, 40)){
                gui.guiItemsDeletions.add(this);
            } else if (isMouseOver(drawStartX, drawStartY-40, drawWidth -5, 40)){
                setClosed(!isClosed());
            }
        }

        return 40;
    }

    public void renderHeader(RoundedShapeRenderer shapeRenderer, SpriteBatch spriteBatch, ShaderProgram fontShader,
                             BitmapFont font, float drawStartX, float drawStartY, float drawWidth,
                             Texture trashTexture, Texture warningTexture, Color headerColor, String headerText, boolean warning){
        shapeRenderer.setColor(headerColor);
        shapeRenderer.roundedRect(drawStartX, drawStartY - 40, drawWidth, 40, 2);
        shapeRenderer.flush();

        spriteBatch.setShader(fontShader);
        spriteBatch.begin();
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, headerText, drawStartX + 5, drawStartY - 5);
        spriteBatch.flush();
        spriteBatch.setShader(null);
        spriteBatch.draw(trashTexture, drawStartX+drawWidth-45, drawStartY-38, trashTexture.getWidth()*(36f/trashTexture.getHeight()), 36);
        if(warning) {
            spriteBatch.draw(warningTexture, drawStartX+drawWidth-10, drawStartY-10, warningTexture.getWidth()*(18f/warningTexture.getHeight()), 18);
        }
    }

    public boolean isMouseOver(int x, int y, int width, int height){
        return Gdx.input.getX() >= x && Gdx.input.getX() <= x + width &&
                Gdx.graphics.getHeight() - Gdx.input.getY() >= y && Gdx.graphics.getHeight() - Gdx.input.getY() <= y + height;
    }
}
