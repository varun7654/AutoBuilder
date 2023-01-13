package com.dacubeking.autobuilder.gui.gui.notification;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Notification {
    private final long deleteTime;
    private final long creationTime;
    private final Color color;
    private final TextBlock notification;

    private static final long ANIMATE_IN_OUT_TIME = 100; //ms

    /**
     * @param color    Color of the background of the notification
     * @param text     Text in the notification
     * @param duration duration of the notification (ms)
     */
    public Notification(Color color, String text, long duration) {
        this.color = color;
        notification = new TextBlock(Fonts.ROBOTO, 30, new TextComponent(text));
        this.creationTime = System.currentTimeMillis();
        this.deleteTime = this.creationTime + (duration);
        AutoBuilder.enableContinuousRendering(this);
    }

    public Notification(Color color, TextBlock text, long duration) {
        this.color = color;
        notification = text;
        this.creationTime = System.currentTimeMillis();
        this.deleteTime = this.creationTime + (duration);
        AutoBuilder.enableContinuousRendering(this);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "deleteTime=" + deleteTime +
                ", creationTime=" + creationTime +
                ", color=" + color +
                ", notification=" + notification +
                '}';
    }

    public boolean tick(ShapeDrawer drawer, Batch batch) {
        long now = System.currentTimeMillis();
        float renderHeight = (Gdx.graphics.getHeight()) + Math.min(Math.min((float) (now - creationTime) / ANIMATE_IN_OUT_TIME,
                (float) (deleteTime - now) / ANIMATE_IN_OUT_TIME), 1) * (-50);
        RoundedShapeRenderer.roundedRect(drawer, Gdx.graphics.getWidth() / 2f - ((notification.getWidth() + 20) / 2f),
                renderHeight, (notification.getWidth() + 20),
                notification.getHeight() + notification.getBottomPaddingAmount(), 5, color);

        FontRenderer.renderText(batch, null, Gdx.graphics.getWidth() / 2f - ((notification.getWidth()) / 2f),
                renderHeight + notification.getBottomPaddingAmount() + notification.getHeight() - notification.getDefaultSize(),
                notification);

        return deleteTime < now;
    }
}
