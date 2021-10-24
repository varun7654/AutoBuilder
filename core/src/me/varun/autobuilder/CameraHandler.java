package me.varun.autobuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.util.MathUtil;
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

    public CameraHandler(@NotNull OrthographicCamera cam, @NotNull InputEventThrower inputEventThrower) {
        this.cam = cam;
        lastMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        oldMouseWorldPos = new Vector3();
        newMouseWorldPos = new Vector3();
        zoomMousePos = new Vector2();
        inputEventThrower.register(this);
        targetX = cam.position.x;
        targetY = cam.position.y;
    }

    public void update(boolean moving, boolean onGui) {
        if (onGui) zoom = lastZoom;
        else lastZoom = zoom;
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());

        /*
        Basically we're getting the world coordinates of the mouse before we zoom and after we zoom. We then find the
        difference between these 2 points and move the camera by that difference. This is so that that the item that is
        under the mouse cursor doesn't move as the camera zooms in and out.
        */

        oldMouseWorldPos.set(zoomMousePos, 0);
        cam.unproject(oldMouseWorldPos);

        cam.zoom = cam.zoom + ((this.zoom - cam.zoom) / (Math.max(1, 0.07f / Gdx.graphics.getDeltaTime()))); //Do Smooth Zoom

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
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !moving) {
            mouseHeldLastFrame = true;
        }

        if (mouseHeldLastFrame && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) { //Left mouse button down. Drag Camera around
            Vector2 deltaPos = mousePos.sub(lastMousePos);
            cam.position.x = cam.position.x - (deltaPos.x * cam.zoom);
            cam.position.y = cam.position.y + (deltaPos.y * cam.zoom);
            cam.update();
            targetX = cam.position.x;
            targetY = cam.position.y;
        } else {
            mouseHeldLastFrame = false;
            cam.position.x = cam.position.x + ((targetX - cam.position.x) / (Math.max(1, 0.1f / Gdx.graphics.getDeltaTime())));
            cam.position.y = cam.position.y + ((targetY - cam.position.y) / (Math.max(1, 0.1f / Gdx.graphics.getDeltaTime())));
            cam.update();
        }

        lastMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (amountY == 1) {
            zoom = zoom * 1.2f;
        } else if (amountY == -1) {
            zoom = zoom * 0.8f;
        }
        MathUtil.clamp(zoom, 0.2, 10);
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
