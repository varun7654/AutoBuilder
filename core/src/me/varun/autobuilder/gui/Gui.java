package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.gui.elements.AddPathButton;
import me.varun.autobuilder.gui.elements.AddScriptButton;
import me.varun.autobuilder.gui.elements.PushAutoButton;
import me.varun.autobuilder.util.MathUntil;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Gui extends InputEventListener {
    private final @NotNull Viewport viewport;
    private final @NotNull BitmapFont font;
    private final @NotNull ShaderProgram fontShader;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    private float smoothScrollPos = 0;
    private float scrollPos = 0;
    private float maxScroll = 0;

    @NotNull AddPathButton addPathButton;
    @NotNull AddScriptButton addScriptButton;
    @NotNull PushAutoButton pushAutoButton;

    private @NotNull Rectangle clipBounds;

    public @NotNull List<AbstractGuiItem> guiItems = new ArrayList<>();

    final @NotNull ExecutorService executorService;

    protected final Texture trashTexture;
    protected final Texture warningTexture;

    private @Nullable TrajectoryItem lastPath = null;

    public Gui(@NotNull Viewport viewport, @NotNull BitmapFont font, @NotNull ShaderProgram fontShader,
               @NotNull InputEventThrower eventThrower, @NotNull ExecutorService executorService, @NotNull CameraHandler cameraHandler){
        this.viewport = viewport;
        this.font = font;
        this.fontShader = fontShader;

        warningTexture = new Texture(Gdx.files.internal("warning.png"), true);
        trashTexture = new Texture(Gdx.files.internal("trash.png"), true);
        warningTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
        trashTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);


        addPathButton = new AddPathButton(0,0, 40, 40, fontShader, font, eventThrower, warningTexture, trashTexture, cameraHandler );
        addScriptButton = new AddScriptButton(0,0, 40, 40, fontShader, font, eventThrower, warningTexture, trashTexture);
        pushAutoButton = new PushAutoButton(0,0, 40, 40);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        eventThrower.register(this);

        this.executorService = executorService;


    }

    Vector2 mouseDownPos = new Vector2();
    boolean dragging = false;
    AbstractGuiItem draggingElement = null;
    int newDraggingElementIndex = 0;
    int oldDraggingElementIndex = 0;

    public void render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, @NotNull Camera camera) {

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.roundedRect(panelX, panelY, panelWidth, panelHeight, 5);

        addScriptButton.render(shapeRenderer, spriteBatch);
        addPathButton.render(shapeRenderer, spriteBatch);
        pushAutoButton.render(shapeRenderer, spriteBatch);

        Rectangle scissors = new Rectangle();

        ScissorStack.calculateScissors(camera, shapeRenderer.getTransformMatrix(), clipBounds, scissors );
        boolean pop = ScissorStack.pushScissors(scissors);

        int yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;

        lastPath = null;
        boolean elementDrawn = false;
        for (int i = 0; i < guiItems.size(); i++) {
            AbstractGuiItem guiItem = guiItems.get(i);
            int newYPos = yPos;

            if(!(draggingElement == null) && !elementDrawn && newYPos < Gdx.graphics.getHeight() - Gdx.input.getY()){
                newYPos = yPos = yPos - 10 - draggingElement.render(shapeRenderer, spriteBatch, Gdx.input.getX() - (panelWidth - 20)/2,
                        (Gdx.graphics.getHeight() - Gdx.input.getY())+20, panelWidth - 20,this);
                newDraggingElementIndex = i;
                elementDrawn = true;
            }

            if(!(guiItem == draggingElement)){
                newYPos = yPos - 10 - guiItem.render(shapeRenderer, spriteBatch, panelX + 10 , yPos, panelWidth - 20,this);
            }

            if(guiItem instanceof TrajectoryItem){
                lastPath = (TrajectoryItem) guiItem;
            }

            if(dragging && draggingElement == null && guiItem.isMouseOver(panelX + 10, yPos-40, panelWidth - 20 -45, 40)){
                draggingElement = guiItem;
                draggingElement.setClosed(true);
                oldDraggingElementIndex = i;
            }
            yPos = newYPos;
        }

        if(!(draggingElement == null) && !elementDrawn ){
            draggingElement.render(shapeRenderer, spriteBatch, Gdx.input.getX() - (panelWidth - 20)/2,
                    (Gdx.graphics.getHeight() - Gdx.input.getY())+20, panelWidth - 20,this);
            newDraggingElementIndex = guiItems.size();
        }


        shapeRenderer.flush();


        if (pop) {
            ScissorStack.popScissors();
        }

        maxScroll = Math.max(0, -(yPos - (int) smoothScrollPos - 10));
        //System.out.println(maxScroll);
    }

    public ArrayList<AbstractGuiItem> guiItemsDeletions = new ArrayList<>();

    public boolean update(){
        for (AbstractGuiItem guiItemsDeletion : guiItemsDeletions) {
            guiItemsDeletion.dispose();
            guiItems.remove(guiItemsDeletion);
        }
        guiItemsDeletions.clear();

        scrollPos = MathUntil.clamp(scrollPos, 0, maxScroll);
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos)/Math.max(1,0.05/Gdx.graphics.getDeltaTime()));

        boolean onGui = false;

        //TODO: Store in a list and iterate over the list
        onGui = addScriptButton.checkHover();
        onGui = onGui | addPathButton.checkHover();
        onGui = onGui | pushAutoButton.checkHover();

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            addScriptButton.checkClick(this);
            addPathButton.checkClick(this);
            pushAutoButton.checkClick(this);
        }

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            mouseDownPos.x = Gdx.input.getX();
            mouseDownPos.y = Gdx.input.getY();
            draggingElement = null;
        } else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            if(MathUntil.len2(mouseDownPos, Gdx.input.getX(), Gdx.input.getY()) > 100){
                dragging = true;
            }
        } else {
            dragging = false;
            if(draggingElement != null){
                System.out.println(newDraggingElementIndex);
                guiItems.remove(oldDraggingElementIndex);
                if(newDraggingElementIndex>oldDraggingElementIndex) newDraggingElementIndex--;
                guiItems.add(newDraggingElementIndex, draggingElement);
                draggingElement = null;
            }
        }

        if(Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.input.getY() > panelY && Gdx.input.getY() < panelY + panelHeight ){
            return true;
        }

        return onGui;
    }

    public void updateScreen(int width, int height){
        panelX = (width-410);
        panelY = 10;
        panelWidth = 400;
        panelHeight = height - 20;

        addPathButton.setX(panelX - 10 - addPathButton.getWidth());
        addPathButton.setY(10);
        addScriptButton.setX(addPathButton.getX() - 10 - addScriptButton.getWidth());
        addScriptButton.setY(10);
        pushAutoButton.setX(addScriptButton.getX() - 10 - pushAutoButton.getWidth());
        pushAutoButton.setY(10);

        clipBounds = new Rectangle(pushAutoButton.getX()-500, panelY, panelWidth + panelX - pushAutoButton.getX()+500,
                panelHeight);
    }

    public void dispose() {
        for (AbstractGuiItem guiItem : guiItems) {
            guiItem.dispose();
        }
        trashTexture.dispose();
        warningTexture.dispose();
        addPathButton.dispose();
        addScriptButton.dispose();
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if(Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.graphics.getHeight() - Gdx.input.getY() > panelY && Gdx.graphics.getHeight() - Gdx.input.getY() < panelY + panelHeight ){
            scrollPos = scrollPos + amountY * 80;
        }
    }

    @NotNull Color color = new Color();

    {
        color.fromHsv(1, 1, 1);
    }

    public @NotNull Color getNextColor(){
        float[] colorHsv = new float[3];
        color.toHsv(colorHsv);
        colorHsv[0] = (colorHsv[0] + 37) % 360;
        Color color = new Color();
        return this.color = color.fromHsv(colorHsv);
    }


    public @Nullable TrajectoryItem getLastPath() {
        return lastPath;
    }



}
