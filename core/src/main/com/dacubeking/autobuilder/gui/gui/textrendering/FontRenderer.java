package com.dacubeking.autobuilder.gui.gui.textrendering;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class FontRenderer {
    public static void renderText(@NotNull Batch batch, @Nullable ShapeDrawer shapeDrawer, float x, float y, @NotNull Fonts font,
                                  int fontSize,
                                  @NotNull TextComponent... textComponents) {
        TextBlock textBlock = new TextBlock(font, fontSize, textComponents);
        renderText(batch, shapeDrawer, x, y, textBlock);
    }

    /**
     * Renders the text block at the specified position.
     *
     * @param batch       The batch to render the text block to.
     * @param shapeDrawer The shape drawer to use for drawing the text block. If the shapeDrawer is null, underlines &
     *                    strikethroughs will not be drawn.
     * @param x           The x position to render the text block at.
     * @param y           The y position to render the text block at.
     * @param textBlock   The text block to render.
     */
    public static void renderText(@NotNull Batch batch, @Nullable ShapeDrawer shapeDrawer, float x, float y,
                                  @NotNull TextBlock textBlock) {
        for (RenderableTextComponent renderableTextComponent : textBlock.getRenderableTextComponents()) {
            BitmapFont fontToUse = renderableTextComponent.getBitmapFont();
            fontToUse.setColor(renderableTextComponent.color);
            fontToUse.draw(batch, renderableTextComponent.text.replace("\n", ""), renderableTextComponent.x + x,
                    renderableTextComponent.y + y + fontToUse.getCapHeight());

            if (shapeDrawer != null) {
                if (renderableTextComponent.isUnderlined && renderableTextComponent.underlineColor.a > 0) {
                    shapeDrawer.line(renderableTextComponent.x + x, renderableTextComponent.y + y,
                            renderableTextComponent.endX + x, renderableTextComponent.y + y,
                            renderableTextComponent.underlineColor, 2);
                }

                if (renderableTextComponent.isStrikethrough && renderableTextComponent.strikethroughColor.a > 0) {
                    shapeDrawer.line(renderableTextComponent.x + x, renderableTextComponent.y + y + fontToUse.getCapHeight() / 2,
                            renderableTextComponent.endX + x, renderableTextComponent.y + y + fontToUse.getCapHeight() / 2,
                            renderableTextComponent.strikethroughColor, 2);
                }
            }
        }
    }
}
