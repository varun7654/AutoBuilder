package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.events.input.InputEventListener;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;
import com.dacubeking.autobuilder.gui.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.dacubeking.autobuilder.gui.gui.GuiConstants.BUTTON_SIZE;
import static com.dacubeking.autobuilder.gui.gui.GuiConstants.BUTTON_SPACING;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseX;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseY;

/**
 * A generic gui that has common functionality that allows it to be scrolled.
 */
public abstract class ScrollableGui extends InputEventListener implements Disposable {

    public final @NotNull AbstractGuiButton openButton;
    protected boolean panelOpen;
    protected float panelX;
    protected float panelY;
    protected float panelWidth;
    protected float panelHeight;
    protected @NotNull Rectangle clipBounds;
    protected float scrollPos;
    protected float smoothScrollPos;

    private final @Nullable ScrollableGui previousElement;

    public ScrollableGui(@NotNull AbstractGuiButton openButton, @Nullable ScrollableGui previousElement) {
        this.openButton = openButton;
        this.previousElement = previousElement;
        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        InputEventThrower.register(this);
    }

    public void render(ShapeDrawer shapeDrawer, Batch batch) {
        openButton.render(shapeDrawer, batch);
    }

    public void updateScreen(int width, int height) {
        if (previousElement == null) {
            openButton.setPosition(BUTTON_SPACING, BUTTON_SPACING);
        } else {
            openButton.setPosition(previousElement.openButton.getRightX() + BUTTON_SPACING, BUTTON_SPACING);
        }
        openButton.setSize(BUTTON_SIZE, BUTTON_SIZE);

        panelX = 10;
        panelY = openButton.getTopY() + BUTTON_SPACING;
        panelWidth = 325;
        panelHeight = height - openButton.getTopY() - BUTTON_SPACING * 2;
        clipBounds = new Rectangle(panelX, panelY, Gdx.graphics.getWidth(), panelHeight);
    }

    public void dispose() {
        openButton.dispose();
        InputEventThrower.unRegister(this);
    }

    public void update(float maxScrollPos) {
        openButton.checkHover();
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (openButton.checkClick()) {
                panelOpen = !panelOpen;
                scrollPos = 0;
                smoothScrollPos = 0;
            } else if (!(getMouseX() >= panelX && getMouseX() <= panelX + panelWidth &&
                    getMouseY() >= panelY && getMouseY() <= panelY + panelHeight)) {
                //We clicked outside the panel
                panelOpen = false;
            }
        }

        scrollPos = MathUtil.clamp(scrollPos, 0, Math.max(0, maxScrollPos - panelHeight));
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos) / Math.max(1,
                0.05 / AutoBuilder.getDeltaTime()));

        if (Math.abs(smoothScrollPos - scrollPos) < 1e-2) {
            AutoBuilder.disableContinuousRendering(this);
        } else {
            AutoBuilder.enableContinuousRendering(this);
        }
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (getMouseX() > panelX && getMouseX() < panelX + panelWidth &&
                getMouseY() > panelY && getMouseY() < panelY + panelHeight) {
            scrollPos = scrollPos + amountY * 80;
        }
    }

    public boolean isPanelOpen() {
        return panelOpen;
    }
}
