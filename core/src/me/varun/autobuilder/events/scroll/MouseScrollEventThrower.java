package me.varun.autobuilder.events.scroll;

import com.badlogic.gdx.InputProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MouseScrollEventThrower implements InputProcessor {

    @NotNull ArrayList<InputEventHandler> eventHandlers = new ArrayList<>();

    public void register(@NotNull InputEventHandler eventHandler){
        eventHandlers.add(eventHandler);
    }

    public boolean unRegister(@NotNull InputEventHandler eventHandler){
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
        for (InputEventHandler eventHandler : eventHandlers) {
            eventHandler.onKeyType(character);
        }
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
        for (InputEventHandler eventHandler : eventHandlers) {
            eventHandler.onScroll(amountX, amountY);
        }
        return false;
    }
}
