package me.varun.autobuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.events.scroll.MouseScrollEventHandler;
import me.varun.autobuilder.events.scroll.MouseScrollEventThrower;
import me.varun.autobuilder.util.MathUntil;

public class CameraHandler implements MouseScrollEventHandler {

    private final OrthographicCamera cam;

    Vector2 lastMousePos;
    Vector2 mousePos;

    Vector3 oldMouseWorldPos;
    Vector3 newMouseWorldPos;

    float zoom = 1;
    float lastZoom = 1;
    Vector2 zoomMousePos;

    float zoomXChange;
    float zoomYChange;

    public CameraHandler(OrthographicCamera cam, MouseScrollEventThrower mouseScrollEventThrower){
        this.cam = cam;
        lastMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        oldMouseWorldPos = new Vector3();
        newMouseWorldPos = new Vector3();
        zoomMousePos = new Vector2();
        mouseScrollEventThrower.register(this);
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

        zoomXChange = newMouseWorldPos.x - oldMouseWorldPos.x;
        zoomYChange = newMouseWorldPos.y - oldMouseWorldPos.y;

        cam.position.x = cam.position.x - zoomXChange;
        cam.position.y = cam.position.y - zoomYChange;
        cam.update();

        if(!moving && Gdx.input.isButtonPressed(Input.Buttons.LEFT)){ //Left mouse button down. Drag Camera around
            Vector2 deltaPos = mousePos.sub(lastMousePos);
            cam.position.x = cam.position.x - (deltaPos.x*cam.zoom);
            cam.position.y = cam.position.y + (deltaPos.y*cam.zoom);
            cam.update();
        }



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
    }
}
