package me.varun.autobuilder.gui.notification;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import me.varun.autobuilder.gui.textrendering.FontRenderer;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Notification {
    private final long deleteTime;
    private final long creationTime;
    private final Color color;
    private final String text;

    private static final long ANIMATE_IN_OUT_TIME = 100; //ns

    /**
     *
     * @param color Color of the background of the notification
     * @param text Text in the notification
     * @param duration duration of the notification (ms)
     */
    public Notification(Color color, String text, long duration){
        this.color = color;
        this.text = text;
        this.creationTime = System.currentTimeMillis();
        this.deleteTime = this.creationTime + (duration);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "deleteTime=" + deleteTime +
                ", creationTime=" + creationTime +
                ", color=" + color +
                ", text='" + text + '\'' +
                '}';
    }

    public boolean tick(ShapeDrawer drawer, Batch batch) {
        long now = System.currentTimeMillis();
        float renderHeight = (Gdx.graphics.getHeight()) + Math.min(
                Math.min((float) (now - creationTime) / ANIMATE_IN_OUT_TIME, (float) (deleteTime - now) / ANIMATE_IN_OUT_TIME),
                1) * (-50);
        RoundedShapeRenderer.roundedRect(drawer, Gdx.graphics.getWidth() / 2f - (700 / 2f), renderHeight, 700, 40, 5, color);

        FontRenderer.renderText(batch, Gdx.graphics.getWidth() / 2f - (700 / 2f), renderHeight + 30, Fonts.ROBOTO, 36,
                new TextComponent(text).setColor(Color.WHITE));

        return deleteTime < now;
    }
}
