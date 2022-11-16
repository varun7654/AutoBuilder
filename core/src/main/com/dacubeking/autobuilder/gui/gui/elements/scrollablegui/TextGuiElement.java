package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.isMouseOver;

/**
 * A {@link GuiElement} that renders text.
 */
public class TextGuiElement implements GuiElement {

    /**
     * @param textComponent The text to render.
     */
    public TextGuiElement(TextComponent... textComponent) {
        text = new TextBlock(Fonts.ROBOTO, 17, textComponent);
    }

    public final TextBlock text;
    public @Nullable TextBlock hoverText = null;
    public @Nullable Runnable onClick = null;

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        text.setWrapWidth(drawWidth - 10);
        FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10,
                drawStartY - text.getTextComponents()[0].size.orElse(text.getDefaultSize()) + 3,
                text);

        if (isMouseOver(drawStartX, drawStartY - text.getHeight(), drawWidth, text.getHeight())) {
            if (hoverText != null) {
                HoverManager.setHoverText(hoverText);
            }

            if (isLeftMouseJustUnpressed) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        }
        return text.getHeight();
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        text.setWrapWidth(drawWidth - 10);
        return text.getHeight();
    }

    /**
     * @param hoverText The text to display when the mouse is over this element.
     * @return This element.
     */
    public TextGuiElement setHoverText(@Nullable TextBlock hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    /**
     * @param onClick The action to perform when this element is clicked.
     * @return This element.
     */
    public TextGuiElement setOnClick(@Nullable Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void dispose() {
    }
}
