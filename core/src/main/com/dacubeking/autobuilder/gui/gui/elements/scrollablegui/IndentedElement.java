package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndentedElement implements GuiElement {

    private final List<GuiElement> elements = new ArrayList<>();
    int level;
    private static final Color[] indentColors;

    static {
        indentColors = new Color[5];
        for (int i = 0; i < indentColors.length; i++) {
            indentColors[i] = new Color().fromHsv((i * (360f / indentColors.length) + 180) % 360, 0.8f, 1);
            indentColors[i].a = 1f;
        }
    }

    public IndentedElement(int level, @NotNull GuiElement... elements) {
        this.elements.addAll(Arrays.asList(elements));
        this.level = level;
    }

    public IndentedElement(int level, @NotNull List<GuiElement> elements) {
        this.elements.addAll(elements);
        this.level = level;
    }

    private static final float INDENT_WIDTH = 20;

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.render(shapeRenderer, spriteBatch, drawStartX + INDENT_WIDTH, drawY,
                    drawWidth - INDENT_WIDTH, camera, isLeftMouseJustUnpressed);
        }
        shapeRenderer.line(drawStartX + INDENT_WIDTH, drawStartY, drawStartX + INDENT_WIDTH, drawY,
                indentColors[level % indentColors.length], 2);
        return drawStartY - drawY + 5;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.getHeight(drawStartX + level, drawY,
                    drawWidth - INDENT_WIDTH, camera, isLeftMouseJustUnpressed);
        }
        return drawStartY - drawY + 5;
    }

    @Override
    public void dispose() {
        for (GuiElement element : elements) {
            element.dispose();
        }
    }
}
