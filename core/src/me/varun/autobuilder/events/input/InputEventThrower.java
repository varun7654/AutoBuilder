package me.varun.autobuilder.events.input;

import com.badlogic.gdx.InputProcessor;
import me.varun.autobuilder.AutoBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InputEventThrower implements InputProcessor {

    @NotNull ArrayList<InputEventListener> eventHandlers = new ArrayList<>();
    @NotNull ArrayList<InputEventListener> iterableEventHandlers = new ArrayList<>();

    public void register(@NotNull InputEventListener eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void unRegister(@NotNull InputEventListener eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    @Override
    public boolean keyDown(int keycode) {
        AutoBuilder.somethingInputed();
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        AutoBuilder.somethingInputed();
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        AutoBuilder.somethingInputed();
        iterableEventHandlers.clear();
        iterableEventHandlers.addAll(eventHandlers);
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
        AutoBuilder.somethingInputed();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        AutoBuilder.somethingInputed();
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        AutoBuilder.somethingInputed();
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        AutoBuilder.somethingInputed();
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        AutoBuilder.somethingInputed();
        iterableEventHandlers.clear();
        iterableEventHandlers.addAll(eventHandlers);
        try {
            for (InputEventListener eventHandler : iterableEventHandlers) {
                eventHandler.onScroll(amountX, amountY);
            }
        } catch (Exception e) {
            AutoBuilder.handleCrash(e);
        }

        return false;
    }
}
