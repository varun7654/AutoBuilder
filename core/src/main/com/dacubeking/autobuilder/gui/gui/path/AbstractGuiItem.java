package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.UndoHandler;
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

    protected float openHeight = 0;

    public boolean isClosed() {
        return closed;
    }

    public boolean isFullyClosed() {
        return closed && openHeight == 0;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    private final TextBlock headerTextBlock = new TextBlock(Fonts.ROBOTO, 36,
            new TextComponent("headerText").setColor(Color.WHITE));

    @Override
    public void dispose() {
        AutoBuilder.disableContinuousRendering(this);
    }

    abstract public int getOpenHeight();

    public int render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, int drawStartX,
                      int drawStartY, int drawWidth, PathGui pathGui, Camera camera, boolean isLeftMouseJustUnpressed) {
        if (isLeftMouseJustUnpressed) {
            if (isMouseOver(drawStartX + drawWidth - 45, drawStartY - 40, drawWidth - 5, 40)) {
                // Trash Icon (delete)
                pathGui.guiItemsDeletions.add(this);
                AutoBuilder.requestRendering();
                UndoHandler.getInstance().somethingChanged();
            } else if (isMouseOver(drawStartX, drawStartY - 40, drawWidth - 5, 40)) {
                setClosed(!isClosed());
            }
        }
        if (isClosed()) {
            if (openHeight > 0) {
                openHeight -= getOpenHeight() * AutoBuilder.getDeltaTime() * 15;
                AutoBuilder.enableContinuousRendering(this);
            }
            if (openHeight <= 0) {
                openHeight = 0;
                AutoBuilder.disableContinuousRendering(this);
            }
        } else {
            if (openHeight < getOpenHeight()) {
                openHeight += getOpenHeight() * AutoBuilder.getDeltaTime() * 15;
                AutoBuilder.enableContinuousRendering(this);
            }
            if (openHeight >= getOpenHeight()) {
                openHeight = getOpenHeight();
                AutoBuilder.disableContinuousRendering(this);
            }
        }

        Rectangle scissor = new Rectangle();
        ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(),
                new Rectangle(drawStartX, drawStartY + 10, drawWidth + 8, -(openHeight + 50)), scissor);
        spriteBatch.flush();
        if (ScissorStack.pushScissors(scissor)) {
            return 1;
        } else {
            return -1;
        }
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

    public int getHeight() {
        return (int) (40 + openHeight);
    }

    protected void setInitialClosed(boolean closed) {
        this.closed = closed;
        if (closed) {
            openHeight = 0;
        } else {
            openHeight = getOpenHeight();
        }
    }
}
