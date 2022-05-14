package com.dacubeking.autobuilder.gui.pathing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dacubeking.autobuilder.gui.UndoHandler;
import com.dacubeking.autobuilder.gui.events.movablepoint.MovablePointEventHandler;
import com.dacubeking.autobuilder.gui.events.movablepoint.PointClickEvent;
import com.dacubeking.autobuilder.gui.events.movablepoint.PointMoveEvent;
import org.jetbrains.annotations.NotNull;

public class MovablePointRenderer extends PointRenderer {


    private final MovablePointEventHandler eventHandler;
    private final Vector3 startPress = new Vector3();
    private boolean pressed = false;
    private boolean dragStarted = false;

    public MovablePointRenderer(float x, float y, @NotNull Color color, float radius,
                                @NotNull MovablePointEventHandler eventHandler) {
        super(x, y, color, radius);
        this.eventHandler = eventHandler;
    }

    public MovablePointRenderer(@NotNull Vector3 pos, @NotNull Color color, float radius,
                                @NotNull MovablePointEventHandler eventHandler) {
        super(pos, color, radius);
        this.eventHandler = eventHandler;
    }

    public MovablePointRenderer(@NotNull Vector2 pos, @NotNull Color color, float radius,
                                @NotNull MovablePointEventHandler eventHandler) {
        super(pos, color, radius);
        this.eventHandler = eventHandler;
    }

    private long lastClickTime = -1;

    public boolean update(@NotNull OrthographicCamera camera, @NotNull Vector3 mousePos, Vector3 mouseDiff) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (new Vector3(mousePos).sub(getRenderPos3()).len2() < Math.pow(20 * camera.zoom, 2)) {
                PointClickEvent event = new PointClickEvent(getPos2(), this, Gdx.input.isButtonJustPressed(Input.Buttons.LEFT),
                        Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT));
                eventHandler.onPointClick(event);
                this.setPosition(event.getPos());

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    startPress.set(mousePos);
                    pressed = true;
                }
            }
            lastClickTime = System.currentTimeMillis();
        }


        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if ((pressed && mouseDiff.len2() != 0) &&
                    (dragStarted || lastClickTime < System.currentTimeMillis() - 500 ||
                            (new Vector3(startPress).sub(mousePos).len2() > Math.pow(10 * camera.zoom, 2)))) {
                dragStarted = true;
                Vector2 newPos = new Vector2(mousePos.x / config.getPointScaleFactor(),
                        mousePos.y / config.getPointScaleFactor());
                PointMoveEvent event = new PointMoveEvent(this.getPos2(), newPos, this);
                eventHandler.onPointMove(event);
                this.setPosition(event.getNewPos());
            }
        } else {
            if (pressed && dragStarted) {
                UndoHandler.getInstance().somethingChanged();
            }
            pressed = false;
            dragStarted = false;
        }

        return pressed;
    }

    @Override
    public @NotNull String toString() {
        return "MovablePointRenderer{" +
                "eventHandler=" + eventHandler +
                ", x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}
