package com.dacubeking.autobuilder.gui.events.input;

import com.badlogic.gdx.InputProcessor;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InputEventThrower implements InputProcessor {

    @NotNull static final List<InputEventListener> eventHandlers = Collections.synchronizedList(new ArrayList<>());
    @NotNull final List<InputEventListener> iterableEventHandlers = new ArrayList<>();

    public static void register(@NotNull InputEventListener eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public static void unRegister(@NotNull InputEventListener eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    @Override
    public boolean keyDown(int keycode) {
        AutoBuilder.somethingInputted();
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        AutoBuilder.somethingInputted();
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        AutoBuilder.somethingInputted();
        iterableEventHandlers.clear();
        synchronized (eventHandlers) {
            iterableEventHandlers.addAll(eventHandlers);
        }

        try {
            for (InputEventListener eventHandler : iterableEventHandlers) {
                eventHandler.onKeyType(character);
            }
        } catch (Exception e) {
            AutoBuilder.handleCrash(e);
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        AutoBuilder.somethingInputted();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        AutoBuilder.somethingInputted();
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        AutoBuilder.somethingInputted();
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        AutoBuilder.somethingInputted();
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        AutoBuilder.somethingInputted();
        iterableEventHandlers.clear();
        synchronized (eventHandlers) {
            iterableEventHandlers.addAll(eventHandlers);
        }
        try {
            for (InputEventListener eventHandler : iterableEventHandlers) {
                eventHandler.onScroll(amountX, amountY);
            }
        } catch (Exception e) {
            AutoBuilder.handleCrash(e);
        }

        return false;
    }

    public static int getNumEventHandlers() {
        return eventHandlers.size();
    }
}
