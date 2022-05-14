package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.regex.Pattern;

/**
 * A drawable object that can be drawn to the screen.
 */
public abstract class Drawable {
    public final Color color;

    protected Drawable(Color color) {
        this.color = color;
    }

    @Override
    abstract public String toString();

    abstract public void draw(ShapeDrawer drawer, Batch batch);

    protected static final Pattern SPLIT_COMMA_NOT_IN_PAREN = Pattern.compile(",\\s*(?![^()]*\\))");
}
