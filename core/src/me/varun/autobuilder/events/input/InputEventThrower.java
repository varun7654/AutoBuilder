package me.varun.autobuilder.events.input;

import com.badlogic.gdx.InputProcessor;
import me.varun.autobuilder.AutoBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InputEventThrower implements InputProcessor {

    @NotNull ArrayList<InputEventListener> eventHandlers = new ArrayList<>();
    @NotNull ArrayList<InputEventListener> eventHandlersAdditions = new ArrayList<>();
    @NotNull ArrayList<InputEventListener> eventHandlersDeletions = new ArrayList<>();

    public void register(@NotNull InputEventListener eventHandler) {
        eventHandlersAdditions.add(eventHandler);
    }

    public void unRegister(@NotNull InputEventListener eventHandler) {
        eventHandlersDeletions.add(eventHandler);
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
        eventHandlers.addAll(eventHandlersAdditions);
        eventHandlers.removeAll(eventHandlersDeletions);
        eventHandlersAdditions.clear();
        eventHandlersDeletions.clear();
        try {
            for (InputEventListener eventHandler : eventHandlers) {
                eventHandler.onKeyType(character);
            }
        } catch (Exception e) {
            AutoBuilder.handleCrash(e);
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
        eventHandlers.addAll(eventHandlersAdditions);
        eventHandlers.removeAll(eventHandlersDeletions);
        eventHandlersAdditions.clear();
        eventHandlersDeletions.clear();
        try {
            for (InputEventListener eventHandler : eventHandlers) {
                eventHandler.onScroll(amountX, amountY);
            }
        } catch (Exception e) {
            AutoBuilder.handleCrash(e);
        }

        return false;
    }
}
