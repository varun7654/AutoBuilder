package com.dacubeking.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dacubeking.autobuilder.gui.events.input.InputEventListener;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.util.MathUtil;
import org.jetbrains.annotations.NotNull;

public class CameraHandler extends InputEventListener {

    private final @NotNull OrthographicCamera cam;

    @NotNull Vector2 lastMousePos;
    @NotNull Vector2 mousePos;

    @NotNull Vector3 oldMouseWorldPos;
    @NotNull Vector3 newMouseWorldPos;

    float zoom = 1;
    float lastZoom = 1;
    @NotNull Vector2 zoomMousePos;

    boolean mouseHeldLastFrame = false;

    float targetX;
    float targetY;
    Vector3 worldPosOfTargetScreenPos = new Vector3();

    public CameraHandler(@NotNull OrthographicCamera cam) {
        this.cam = cam;
        lastMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        oldMouseWorldPos = new Vector3();
        newMouseWorldPos = new Vector3();
        zoomMousePos = new Vector2();
        InputEventThrower.register(this);
        targetX = cam.position.x;
        targetY = cam.position.y;
    }

    public void update(boolean moving, boolean onGui) {
        if (Gdx.graphics.getHeight() == 0 || Gdx.graphics.getWidth() == 0) return;
        if (onGui) {
            zoom = lastZoom;
        } else {
            lastZoom = zoom;
        }
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());

        /*
        Basically we're getting the world coordinates of the mouse before we zoom and after we zoom. We then find the
        difference between these 2 points and move the camera by that difference. This is so that the item that is
        under the mouse cursor doesn't move as the camera zooms in and out.
        */

        oldMouseWorldPos.set(zoomMousePos, 0);
        cam.unproject(oldMouseWorldPos);

        cam.zoom = cam.zoom + ((this.zoom - cam.zoom) / (Math.max(1, 0.07f / AutoBuilder.getDeltaTime()))); //Do Smooth Zoom

        cam.update();
        newMouseWorldPos.set(zoomMousePos, 0);
        cam.unproject(newMouseWorldPos);

        float zoomXChange = newMouseWorldPos.x - oldMouseWorldPos.x;
        float zoomYChange = newMouseWorldPos.y - oldMouseWorldPos.y;

        cam.position.x = cam.position.x - zoomXChange;
        cam.position.y = cam.position.y - zoomYChange;
        targetX -= zoomXChange;
        targetY -= zoomYChange;
        cam.update();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !(moving | onGui)) {
            mouseHeldLastFrame = true;
        } else if (moving) {
            mouseHeldLastFrame = false;
        }

        if (mouseHeldLastFrame && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) { // Drag Camera around
            Vector2 deltaPos = mousePos.sub(lastMousePos);
            cam.position.x = cam.position.x - (deltaPos.x * cam.zoom * (720f / Gdx.graphics.getHeight()));
            cam.position.y = cam.position.y + (deltaPos.y * cam.zoom * (720f / Gdx.graphics.getHeight()));
            cam.update();
            targetX = cam.position.x;
            targetY = cam.position.y;
        } else {
            // Smoothly move camera to target position
            mouseHeldLastFrame = false;
            cam.position.x = cam.position.x + ((targetX - cam.position.x) / (Math.max(1, 0.1f / AutoBuilder.getDeltaTime())));
            cam.position.y = cam.position.y + ((targetY - cam.position.y) / (Math.max(1, 0.1f / AutoBuilder.getDeltaTime())));
            cam.update();
        }

        lastMousePos.set(Gdx.input.getX(), Gdx.input.getY());

        if (Math.abs(targetX - cam.position.x) < 1e-2 && Math.abs(targetY - cam.position.y) < 1e-2 && Math.abs(
                this.zoom - cam.zoom) < 1e-4) {
            AutoBuilder.disableContinuousRendering(this);
        } else {
            AutoBuilder.enableContinuousRendering(this);
        }
    }


    @Override
    public void onScroll(float amountX, float amountY) {
        zoom = zoom * (1 + MathUtil.clamp(amountY / 5, -0.5f, 0.5f));

        zoom = MathUtil.clamp(zoom, 0.2f, 10f);
        zoomMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }

    public void ensureOnScreen(Vector3 worldPos) {
        Vector3 screenPos = new Vector3(worldPos);
        cam.project(screenPos); //Get chordates of the point in relation to the screen


        //Set some default values that will result in the screen not moving
        float targetScreenX = screenPos.x;
        float targetScreenY = Gdx.graphics.getHeight() - screenPos.y;

        //Check screen bounds and if were outside of it set a target screen pos thats inside the screen
        if (screenPos.x < 25) {
            targetScreenX = 50;
        } else if (screenPos.x > Gdx.graphics.getWidth() - 500) {
            targetScreenX = Gdx.graphics.getWidth() - 525;
        }

        if (screenPos.y < 25) {
            targetScreenY = Gdx.graphics.getHeight() - 50;
        } else if (screenPos.y > Gdx.graphics.getHeight() - 25) {
            targetScreenY = 50;
        }

        worldPosOfTargetScreenPos.set(targetScreenX, targetScreenY, 0);
        cam.unproject(worldPosOfTargetScreenPos); //Find the world position of where we want the point on the screen

        //Find the difference between where we want the point (unprojected target screen cords) and where the point is
        //and then add that to the current camera pos
        targetX = cam.position.x + worldPos.x - worldPosOfTargetScreenPos.x;
        targetY = cam.position.y + worldPos.y - worldPosOfTargetScreenPos.y;
    }
}
