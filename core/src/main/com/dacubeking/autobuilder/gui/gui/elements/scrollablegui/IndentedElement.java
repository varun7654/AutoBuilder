package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link GuiElement} that can contain other {@link GuiElement}s that are indented.
 * <p>
 * The elements are rendered in the order that they're in the list. A colored line is rendered to the left of the elements with
 * the color signifying the indentation level.
 */
public class IndentedElement implements GuiElement {

    private final List<GuiElement> elements = new ArrayList<>();
    int level;
    private static final Color[] indentColors;
    private static final float INDENT_WIDTH = 20;

    static {
        // Generate the colors for the indentation lines
        indentColors = new Color[5];
        for (int i = 0; i < indentColors.length; i++) {
            indentColors[i] = new Color().fromHsv((i * (360f / indentColors.length) + 180) % 360, 0.8f, 1);
            indentColors[i].a = 1f;
        }
    }

    /**
     * Creates a new {@link IndentedElement} with the specified indentation level.
     *
     * @param level    The indentation level.
     * @param elements The elements to add to the list.
     * @see IndentedElement
     */
    public IndentedElement(int level, @NotNull GuiElement... elements) {
        this.elements.addAll(Arrays.asList(elements));
        this.level = level;
    }

    /**
     * Creates a new {@link IndentedElement} with the specified indentation level.
     *
     * @param level    The indentation level.
     * @param elements The elements to render.
     * @see IndentedElement
     */
    public IndentedElement(int level, @NotNull List<GuiElement> elements) {
        this.elements.addAll(elements);
        this.level = level;
    }

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
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        float drawY = drawStartY;
        for (GuiElement element : elements) {
            drawY -= element.getHeight(drawStartX + level, drawY,
                    drawWidth - INDENT_WIDTH, isLeftMouseJustUnpressed);
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
