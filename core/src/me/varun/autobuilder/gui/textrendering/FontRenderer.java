package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class FontRenderer {
    private static final GlyphLayout glyphLayout = new GlyphLayout();

    public static void renderText(Batch batch, float x, float y, Fonts font, int fontSize, TextComponent... textComponents) {
        TextBlock textBlock = new TextBlock(font, fontSize, textComponents);
        renderText(batch, x, y, textBlock);
    }

    public static void renderText(Batch batch, float x, float y, TextBlock textBlock) {
        for (RenderableTextComponent renderableTextComponent : textBlock.getRenderableTextComponents()) {
            BitmapFont fontToUse = renderableTextComponent.getBitmapFont();
            fontToUse.setColor(renderableTextComponent.color);
            fontToUse.draw(batch, renderableTextComponent.text.replace("\n", ""), renderableTextComponent.x + x,
                    renderableTextComponent.y + y + fontToUse.getCapHeight());
        }
    }
}
