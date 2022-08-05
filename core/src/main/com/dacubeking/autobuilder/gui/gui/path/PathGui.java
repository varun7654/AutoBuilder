package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.CameraHandler;
import com.dacubeking.autobuilder.gui.UndoHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventListener;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.util.MathUtil;
import com.dacubeking.autobuilder.gui.util.MouseUtil;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import static com.dacubeking.autobuilder.gui.util.MathUtil.dist2;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.*;
import static java.awt.Color.HSBtoRGB;

public class PathGui extends InputEventListener {
    final @NotNull ExecutorService executorService;
    public @NotNull List<AbstractGuiItem> guiItems = Collections.synchronizedList(new ArrayList<>());
    public ArrayList<AbstractGuiItem> guiItemsDeletions = new ArrayList<>();
    @NotNull AddPathButton addPathButton;
    @NotNull AddScriptButton addScriptButton;
    @NotNull PushAutoButton pushAutoButton;
    @NotNull Vector2 mouseDownPos = new Vector2();
    boolean dragging = false;
    @Nullable AbstractGuiItem draggingElement = null;
    int newDraggingElementIndex = 0;
    int oldDraggingElementIndex = 0;
    @NotNull Vector2 dragOffset = new Vector2();
    boolean wasDraggingElementClosed;
    @NotNull Color color = new Color(1, 1, 1, 1);

    private int panelX;
    private int panelY;
    private int panelWidth = 400;
    private int panelHeight;
    private float smoothScrollPos = 0;
    private float scrollPos = 0;
    private float maxScroll = 0;
    private @NotNull Rectangle clipBounds;
    private @Nullable TrajectoryItem lastPath = null;
    private boolean clickedInsidePanel;
    private float previousMaxScroll;
    private long previousMaxScrollTimeSet = 0;

    {
        color.fromHsv(1, 1, 1);
    }

    public PathGui(@NotNull ExecutorService executorService, @NotNull CameraHandler cameraHandler) {


        addPathButton = new AddPathButton(0, 0, 40, 40, cameraHandler);
        addScriptButton = new AddScriptButton(0, 0, 40, 40);
        pushAutoButton = new PushAutoButton(0, 0, 40, 40);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        InputEventThrower.register(this);

        this.executorService = executorService;
    }

    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, @NotNull Camera camera) {
        shapeRenderer.setColor(Color.WHITE);
        RoundedShapeRenderer.roundedRect(shapeRenderer, panelX, panelY, panelWidth, panelHeight, 5);

        addScriptButton.render(shapeRenderer, spriteBatch);
        addPathButton.render(shapeRenderer, spriteBatch);
        pushAutoButton.render(shapeRenderer, spriteBatch);

        Rectangle scissors = new Rectangle();

        ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(), clipBounds, scissors);
        boolean pop = ScissorStack.pushScissors(scissors);

        int yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;

        lastPath = null;
        boolean draggingElementSpotFound = false;
        TrajectoryItem lastDraggingElementTrajectory = null;

        Vector2 mousePos = getMousePos();

        for (int i = 0; i < guiItems.size(); i++) {
            AbstractGuiItem guiItem = guiItems.get(i);

            if (dragging && draggingElement == null && isMouseOver(mouseDownPos, panelX + 10, yPos - 40, panelWidth - 20 - 45,
                    40)) {
                draggingElement = guiItem;
                wasDraggingElementClosed = guiItem.isClosed();
                draggingElement.setClosed(true);
                oldDraggingElementIndex = i;
                dragOffset.set(mouseDownPos.x - panelX - 10, mouseDownPos.y - yPos);
            }

            if (draggingElement != null && !draggingElementSpotFound &&
                    (yPos - (guiItem.getHeight() / 2f) - (newDraggingElementIndex == i ? draggingElement.getHeight() : 0))
                            < getMouseY() - dragOffset.y) {
                yPos = yPos - 10 - draggingElement.getHeight();
                newDraggingElementIndex = i;
                draggingElementSpotFound = true;
                lastDraggingElementTrajectory = lastPath;
                if (draggingElement instanceof TrajectoryItem) {
                    lastPath = (TrajectoryItem) draggingElement;
                }
            }

            if (guiItem != draggingElement) {
                yPos = yPos - 10 - guiItem.render(shapeRenderer, spriteBatch, panelX + 10, yPos, panelWidth - 20, this,
                        camera, isLeftMouseJustUnpressed && dist2(mouseDownPos, mousePos) < 10);
            }

            if (guiItem instanceof TrajectoryItem) {
                lastPath = (TrajectoryItem) guiItem;
            }
        }

        if (draggingElement != null) {
            if (!draggingElementSpotFound) {
                newDraggingElementIndex = guiItems.size();
            }
            lastPath = lastDraggingElementTrajectory;
            draggingElement.render(shapeRenderer, spriteBatch,
                    (int) (getMouseX() - dragOffset.x),
                    (int) (getMouseY() - dragOffset.y),
                    panelWidth - 20, this, camera, false);

            if (getMouseY() < panelY + 15) {
                onScroll(0, 0.1f);
            } else if (getMouseY() > panelY + panelHeight - 15) {
                onScroll(0, -0.1f);
            }
        }


        if (pop) {
            spriteBatch.flush();
            ScissorStack.popScissors();
        }

        maxScroll = Math.max(0, -(yPos - (int) smoothScrollPos - 10) +
                //Add the height of the dragging element
                (draggingElement != null ? draggingElement.getOpenHeight(getGuiItemWidth()) + 40 : 0));
    }

    boolean isLeftMouseJustUnpressed = false;
    boolean clickedOnEdgeOfPanel = false;

    public boolean update() {
        isLeftMouseJustUnpressed = isIsLeftMouseJustUnpressed();

        for (AbstractGuiItem guiItemsDeletion : guiItemsDeletions) {
            guiItemsDeletion.dispose();
            guiItems.remove(guiItemsDeletion);
        }
        guiItemsDeletions.clear();

        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos) /
                Math.max(1, 0.05 / AutoBuilder.getDeltaTime()));

        if (Math.abs(scrollPos - smoothScrollPos) < 1e-2) {
            AutoBuilder.disableContinuousRendering(this);
        } else {
            AutoBuilder.enableContinuousRendering(this);
        }

        boolean onGui = false;

        //TODO: Store in a list and iterate over the list
        onGui = addScriptButton.checkHover();
        onGui = onGui | addPathButton.checkHover();
        onGui = onGui | pushAutoButton.checkHover();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            addScriptButton.checkClick(this);
            addPathButton.checkClick(this);
            pushAutoButton.checkClick(this);
        }

        if (MouseUtil.isMouseOver(panelX, panelY, panelWidth, panelHeight) || clickedInsidePanel) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                mouseDownPos.x = getMouseX();
                mouseDownPos.y = getMouseY();
                draggingElement = null;
                clickedInsidePanel = true;
            } else if (clickedInsidePanel && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                if (MathUtil.len2(mouseDownPos, getMouseX(), getMouseY()) > 100) {
                    dragging = !clickedOnEdgeOfPanel;
                }
            } else {
                clickedInsidePanel = false;
                dragging = false;
                if (draggingElement != null) {
                    isLeftMouseJustUnpressed = false; //Don't register a click so the element doesn't reopen
                    draggingElement.setClosed(wasDraggingElementClosed);
                    guiItems.remove(oldDraggingElementIndex);
                    if (newDraggingElementIndex > oldDraggingElementIndex) newDraggingElementIndex--;
                    guiItems.add(newDraggingElementIndex, draggingElement);
                    draggingElement = null;
                    UndoHandler.getInstance().somethingChanged();
                }
            }

            if (MouseUtil.isMouseOver(panelX, panelY, 9, panelHeight) &&
                    (Gdx.input.isButtonJustPressed(Buttons.LEFT) || !Gdx.input.isButtonJustPressed(Buttons.LEFT))) {
                AutoBuilder.setMouseCursor(SystemCursor.HorizontalResize);
                if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
                    clickedOnEdgeOfPanel = true;
                }
            }

            if (clickedOnEdgeOfPanel && Gdx.input.isButtonPressed(Buttons.LEFT)) {
                AutoBuilder.setMouseCursor(SystemCursor.HorizontalResize);
                panelWidth = Gdx.graphics.getWidth() - getMouseX() - 10;
                AutoBuilder.getInstance().updateScreens(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            } else {
                clickedOnEdgeOfPanel = false;
            }
            return true;
        }

        return onGui;
    }


    public void scrollToBottom() {
        int yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;
        for (AbstractGuiItem guiItem : guiItems) {
            yPos = yPos - 10 - guiItem.getHeight();
        }
        scrollPos = Math.max(0, -(yPos - (int) smoothScrollPos - 10));
    }

    public void updateScreen(int width, int height) {
        panelWidth = Math.min(Math.max(panelWidth, 15), width - 20);
        panelHeight = height - 20;
        panelX = (width - panelWidth - 10);
        panelY = 10;


        addPathButton.setPosition(panelX - 10 - addPathButton.getWidth(), 10);
        addScriptButton.setPosition(addPathButton.getX() - 10 - addScriptButton.getWidth(), 10);
        pushAutoButton.setPosition(addScriptButton.getX() - 10 - pushAutoButton.getWidth(), 10);

        clipBounds = new Rectangle(0, panelY, width, panelHeight);
    }

    public Vector2 getPushAutoButtonPos() {
        return new Vector2(pushAutoButton.getX(), pushAutoButton.getY());
    }

    public int getPanelX() {
        return panelX;
    }

    public int getGuiItemWidth() {
        return panelWidth - 20;
    }

    public void dispose() {
        for (AbstractGuiItem guiItem : guiItems) {
            guiItem.dispose();
        }
        addPathButton.dispose();
        addScriptButton.dispose();
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (getMouseX() > panelX && getMouseX() < panelX + panelWidth &&
                getMouseY() > panelY && getMouseY() < panelY + panelHeight) {
            scrollPos = MathUtil.clamp(scrollPos + amountY * 80, 0, Math.max(scrollPos, maxScroll));
        }
    }


    Random random = new Random();

    public @NotNull Color getNextColor() {
        java.awt.Color colorJava = new java.awt.Color(HSBtoRGB(random.nextFloat(), 1, 1));
        return new Color(colorJava.getRed() / 255f, colorJava.getGreen() / 255f, colorJava.getBlue() / 255f, 1);
    }

    public @Nullable TrajectoryItem getLastPath() {
        return lastPath;
    }
}
