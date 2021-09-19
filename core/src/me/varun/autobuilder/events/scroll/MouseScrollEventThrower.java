package me.varun.autobuilder.events.scroll;

import com.badlogic.gdx.InputProcessor;

import java.util.ArrayList;

public class MouseScrollEventThrower implements InputProcessor {

    ArrayList<MouseScrollEventHandler> eventHandlers = new ArrayList<>();

    public void register(MouseScrollEventHandler eventHandler){
        eventHandlers.add(eventHandler);
    }

    public boolean unRegister(MouseScrollEventHandler eventHandler){
        return eventHandlers.remove(eventHandler);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        for (MouseScrollEventHandler eventHandler : eventHandlers) {
            eventHandler.onScroll(amountX, amountY);
        }
        return false;
    }
}
