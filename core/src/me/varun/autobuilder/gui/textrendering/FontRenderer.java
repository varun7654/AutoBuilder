package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import org.jetbrains.annotations.NotNull;

public class FontRenderer {
    public static void renderText(@NotNull Batch batch, float x, float y, @NotNull Fonts font, int fontSize,
                                  @NotNull TextComponent... textComponents) {
        TextBlock textBlock = new TextBlock(font, fontSize, textComponents);
        renderText(batch, x, y, textBlock);
    }

    public static void renderText(@NotNull Batch batch, float x, float y, @NotNull TextBlock textBlock) {
        for (RenderableTextComponent renderableTextComponent : textBlock.getRenderableTextComponents()) {
            BitmapFont fontToUse = renderableTextComponent.getBitmapFont();
            fontToUse.setColor(renderableTextComponent.color);
            fontToUse.draw(batch, renderableTextComponent.text.replace("\n", ""), renderableTextComponent.x + x,
                    renderableTextComponent.y + y + fontToUse.getCapHeight());
        }
    }
}
