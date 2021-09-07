package me.varun.autobuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.util.MathUntil;

public class CameraHandler implements InputProcessor {

    private final OrthographicCamera cam;

    Vector2 lastMousePos;
    Vector2 mousePos;

    Vector3 oldMouseWorldPos;
    Vector3 newMouseWorldPos;

    float zoom = 1;

    float zoomXChange;
    float zoomYChange;

    public CameraHandler(OrthographicCamera cam){
        this.cam = cam;
        lastMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        oldMouseWorldPos = new Vector3();
        newMouseWorldPos = new Vector3();
    }

    public void update(){
        mousePos.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

        oldMouseWorldPos.set(mousePos, 0);
        cam.unproject(oldMouseWorldPos);

        cam.zoom = cam.zoom + ((this.zoom - cam.zoom)/2); //Do Smooth Zoom

        cam.update();
        newMouseWorldPos.set(mousePos, 0);
        cam.unproject(newMouseWorldPos);

        zoomXChange = newMouseWorldPos.x - oldMouseWorldPos.x;
        zoomYChange = newMouseWorldPos.y - oldMouseWorldPos.y;

        cam.position.x = cam.position.x - zoomXChange;
        cam.position.y = cam.position.y + zoomYChange;
        cam.update();

        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){ //Left mouse button down
            Vector2 deltaPos = mousePos.sub(lastMousePos);
            cam.position.x = cam.position.x - (deltaPos.x*cam.zoom);
            cam.position.y = cam.position.y - (deltaPos.y*cam.zoom);
            cam.update();
        }



        lastMousePos.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }


    /**
     * Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    /**
     * Called when a key was released
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    /**
     * Called when a key was typed
     *
     * @param character The character
     * @return whether the input was processed
     */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * Called when the screen was touched or a mouse button was pressed. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     *
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return whether the input was processed
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return whether the input was processed
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when a finger or the mouse was dragged.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @return whether the input was processed
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
     *
     * @param screenX
     * @param screenY
     * @return whether the input was processed
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Called when the mouse wheel was scrolled. Will not be called on iOS.
     *
     * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @return whether the input was processed.
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(amountY == 1){
            zoom = zoom * 1.2f;
        } else if (amountY == - 1){
            zoom = zoom * 0.8f;
        }

        MathUntil.clamp(zoom, 0.2, 10);

        return false;
    }
}
