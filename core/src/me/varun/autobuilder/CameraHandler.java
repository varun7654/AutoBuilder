package me.varun.autobuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.util.MathUntil;
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

    public CameraHandler(@NotNull OrthographicCamera cam, @NotNull InputEventThrower inputEventThrower){
        this.cam = cam;
        lastMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        oldMouseWorldPos = new Vector3();
        newMouseWorldPos = new Vector3();
        zoomMousePos = new Vector2();
        inputEventThrower.register(this);
    }

    public void update(boolean moving, boolean onGui){
        if(onGui) zoom = lastZoom; else lastZoom = zoom;
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());

        /*
        Basically we're getting the world coordinates of the mouse before we zoom and after we zoom. We then find the
        difference between these 2 points and move the camera by that difference. This is so that that the item that is
        under the mouse cursor doesn't move as the camera zooms in and out.
        */

        oldMouseWorldPos.set(zoomMousePos, 0);
        cam.unproject(oldMouseWorldPos);

        cam.zoom = cam.zoom + ((this.zoom - cam.zoom)/(Math.max(1,0.07f/Gdx.graphics.getDeltaTime()))); //Do Smooth Zoom

        cam.update();
        newMouseWorldPos.set(zoomMousePos, 0);
        cam.unproject(newMouseWorldPos);

        float zoomXChange = newMouseWorldPos.x - oldMouseWorldPos.x;
        float zoomYChange = newMouseWorldPos.y - oldMouseWorldPos.y;

        cam.position.x = cam.position.x - zoomXChange;
        cam.position.y = cam.position.y - zoomYChange;
        cam.update();

        if((!moving || mouseHeldLastFrame)&& Gdx.input.isButtonPressed(Input.Buttons.LEFT)){ //Left mouse button down. Drag Camera around
            Vector2 deltaPos = mousePos.sub(lastMousePos);
            cam.position.x = cam.position.x - (deltaPos.x*cam.zoom);
            cam.position.y = cam.position.y + (deltaPos.y*cam.zoom);
            cam.update();
            mouseHeldLastFrame = true;
            targetX = cam.position.x;
            targetY = cam.position.y;
        } else {
            mouseHeldLastFrame = false;
        }

        //TODO Implement smooth cam movement

        lastMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }



    @Override
    public void onScroll(float amountX, float amountY) {
        if(amountY == 1){
            zoom = zoom * 1.2f;
        } else if (amountY == - 1){
            zoom = zoom * 0.8f;
        }
        MathUntil.clamp(zoom, 0.2, 10);
        zoomMousePos.set(Gdx.input.getX(), Gdx.input.getY());
    }

    public boolean ensureOnScreen(Vector3 worldPos){
        cam.project(worldPos); //World Pos is now screen cords
        float correctionX = 0;
        float correctionY = 0;
        if(worldPos.x < 10 ){
            correctionX = 30 - worldPos.x;
        } else if(worldPos.x > Gdx.graphics.getWidth() - 10){
            correctionX =  worldPos.x - Gdx.graphics.getWidth() + 30;
        }

        if(worldPos.y < 10 ){
            correctionY = 30 - worldPos.y;
        } else if( worldPos.y > Gdx.graphics.getHeight()){
            correctionY =  worldPos.y - Gdx.graphics.getHeight() + 30;
        }

        return false;
    }
}
