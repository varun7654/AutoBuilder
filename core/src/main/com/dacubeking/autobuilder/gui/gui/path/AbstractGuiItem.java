package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.isMouseOver;

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

    private final TextBlock headerTextBlock = new TextBlock(Fonts.ROBOTO, 36,
            new TextComponent("headerText").setColor(Color.WHITE));

    @Override
    abstract public void dispose();

    abstract public int getHeight();

    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX,
                      int drawStartY, int drawWidth, PathGui pathGui, boolean isLeftMouseJustUnpressed) {
        if (isLeftMouseJustUnpressed) {
            if (isMouseOver(drawStartX + drawWidth - 45, drawStartY - 40, drawWidth - 5, 40)) {
                pathGui.guiItemsDeletions.add(this);
            } else if (isMouseOver(drawStartX, drawStartY - 40, drawWidth - 5, 40)) {
                setClosed(!isClosed());
            }
        }

        return 40;
    }

    public void renderHeader(ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                             float drawStartY, float drawWidth, Texture trashTexture, Texture warningTexture,
                             Color headerColor, String headerText, boolean warning) {
        shapeRenderer.setColor(headerColor);
        RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX, drawStartY - 40, drawWidth, 40, 2, headerColor);

        headerTextBlock.setTextInComponent(0, headerText);
        FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 5, drawStartY - 31, headerTextBlock);
        spriteBatch.draw(trashTexture, drawStartX + drawWidth - 45, drawStartY - 38,
                trashTexture.getWidth() * (36f / trashTexture.getHeight()), 36);
        if (warning) {
            spriteBatch.draw(warningTexture, drawStartX + drawWidth - 10, drawStartY - 10,
                    warningTexture.getWidth() * (18f / warningTexture.getHeight()), 18);
        }
    }
}
