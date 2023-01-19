package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.Colors;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.isMouseOver;

public class ButtonGuiElement implements GuiElement {

    private final TextBlock text;
    private final Runnable onClick;
    private @Nullable TextBlock hoverText = null;

    public ButtonGuiElement(Runnable onClick, TextComponent... textComponents) {
        this.text = new TextBlock(Fonts.ROBOTO, 20, textComponents);
        this.onClick = onClick;
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        var textHeight = text.getHeight();
        var textWidth = text.getWidth();

        boolean isMouseOver = isMouseOver(drawStartX + 10, drawStartY - textHeight - 6, textWidth + 10, textHeight + 6);

        if (isMouseOver) {
            shapeRenderer.setColor(Colors.LIGHT_GREY);
            if (hoverText != null) {
                HoverManager.setHoverText(hoverText);
            }

            if (isLeftMouseJustUnpressed) {
                onClick.run();
            }
        } else {
            shapeRenderer.setColor(Colors.LIGHTER_GREY);
        }

        RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX + 10, drawStartY - textHeight - 6,
                textWidth + 10, textHeight + 6, 3);

        FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 15,
                drawStartY - textHeight + 2, text);
        return textHeight + 6;
    }


    /**
     * @param hoverText The text to display when the mouse is over this element.
     * @return This element.
     */
    public ButtonGuiElement setHoverText(@Nullable TextBlock hoverText) {
        this.hoverText = hoverText;
        return this;
    }


    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        return text.getHeight() + 6;
    }

    @Override
    public void dispose() {

    }
}
