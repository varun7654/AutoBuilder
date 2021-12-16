package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import me.varun.autobuilder.gui.textrendering.FontRenderer;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class AbstractGuiItem implements Disposable {

    protected static final @NotNull Color LIGHT_GREY = Color.valueOf("E9E9E9");
    static protected final Texture trashTexture;
    static protected final Texture warningTexture;

    static {
        warningTexture = new Texture(Gdx.files.internal("warning.png"), true);
        trashTexture = new Texture(Gdx.files.internal("trash.png"), true);
        warningTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
        trashTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
    }

    private boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    abstract public void dispose();

    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, PathGui pathGui) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (isMouseOver(drawStartX + drawWidth - 45, drawStartY - 40, drawWidth - 5, 40)) {
                pathGui.guiItemsDeletions.add(this);
            } else if (isMouseOver(drawStartX, drawStartY - 40, drawWidth - 5, 40)) {
                setClosed(!isClosed());
            }
        }

        return 40;
    }

    public void renderHeader(ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, ShaderProgram fontShader,
                             BitmapFont font, float drawStartX, float drawStartY, float drawWidth,
                             Texture trashTexture, Texture warningTexture, Color headerColor, String headerText, boolean warning) {
        shapeRenderer.setColor(headerColor);
        //System.out.println(headerColor);
        RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX, drawStartY - 40, drawWidth, 40, 2, headerColor);

        spriteBatch.setShader(fontShader);
        font.getData().setScale(0.6f);
        font.setColor(Color.WHITE);
        //font.draw(spriteBatch, headerText, drawStartX + 5, drawStartY - 5);
        FontRenderer.renderText(spriteBatch, drawStartX + 5, drawStartY - 5, new TextBlock(Fonts.ROBOTO, 36,
                new TextComponent(headerText).setColor(Color.WHITE)));
        spriteBatch.setShader(null);
        spriteBatch.draw(trashTexture, drawStartX + drawWidth - 45, drawStartY - 38,
                trashTexture.getWidth() * (36f / trashTexture.getHeight()), 36);
        if (warning) {
            spriteBatch.draw(warningTexture, drawStartX + drawWidth - 10, drawStartY - 10,
                    warningTexture.getWidth() * (18f / warningTexture.getHeight()), 18);
        }
    }

    public boolean isMouseOver(int x, int y, int width, int height) {
        return Gdx.input.getX() >= x && Gdx.input.getX() <= x + width &&
                Gdx.graphics.getHeight() - Gdx.input.getY() >= y && Gdx.graphics.getHeight() - Gdx.input.getY() <= y + height;
    }
}
