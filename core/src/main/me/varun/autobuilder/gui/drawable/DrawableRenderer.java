package me.varun.autobuilder.gui.drawable;

import com.badlogic.gdx.graphics.g2d.Batch;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DrawableRenderer {

    private final List<Drawable> drawables = new ArrayList<>();

    synchronized public void setDrawables(Collection<Drawable> drawables) {
        this.drawables.clear();
        this.drawables.addAll(drawables);
    }


    synchronized public void render(ShapeDrawer drawer, Batch batch) {
        for (Drawable drawable : drawables) {
            drawable.draw(drawer, batch);
        }
    }
}
