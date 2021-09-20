package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.events.scroll.InputEventHandler;
import me.varun.autobuilder.events.scroll.MouseScrollEventThrower;
import me.varun.autobuilder.util.MathUntil;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Gui extends InputEventHandler {
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

    private @NotNull Rectangle clipBounds;

    public @NotNull List<AbstractGuiItem> guiItems = new ArrayList<>();

    final @NotNull ExecutorService executorService;

    public Gui(@NotNull Viewport viewport, @NotNull BitmapFont font, @NotNull ShaderProgram fontShader,
               @NotNull MouseScrollEventThrower eventThrower, @NotNull ExecutorService executorService){
        this.viewport = viewport;
        this.font = font;
        this.fontShader = fontShader;

        addPathButton = new AddPathButton(0,0, 40, 40, fontShader, font);
        addScriptButton = new AddScriptButton(0,0, 40, 40, fontShader, font);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        eventThrower.register(this);

        this.executorService = executorService;


    }

    public void render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, @NotNull Camera camera) {

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.roundedRect(panelX, panelY, panelWidth, panelHeight, 5);

        addScriptButton.render(shapeRenderer, spriteBatch);
        addPathButton.render(shapeRenderer, spriteBatch);

        //spriteBatch.draw(new Texture("path_icon.png"), 1, 1);

        Rectangle scissors = new Rectangle();

        ScissorStack.calculateScissors(camera, shapeRenderer.getTransformMatrix(), clipBounds, scissors );
        boolean pop = ScissorStack.pushScissors(scissors);

        int yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;

        for (AbstractGuiItem guiItem : guiItems) {
            yPos = yPos - 10 - guiItem.render(shapeRenderer, spriteBatch, panelX + 10 , yPos, panelWidth - 20);
        }
        shapeRenderer.flush();


        if (pop) {
            ScissorStack.popScissors();
        }

        maxScroll = Math.max(0, -(yPos - (int) smoothScrollPos - 10));
        //System.out.println(maxScroll);
    }

    public boolean update(){
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos)/Math.max(1,0.05/Gdx.graphics.getDeltaTime()));

        boolean onGui = false;

        //TODO: Store in a list and iterate over the list
        onGui = onGui | addScriptButton.checkHover();
        onGui = onGui | addPathButton.checkHover();

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            addScriptButton.checkClick(this);
            addPathButton.checkClick(this);
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

        clipBounds = new Rectangle(addScriptButton.getX(), panelY, panelWidth + panelX - addScriptButton.getX(),
                panelHeight);
    }

    public void dispose() {
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if(Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.graphics.getHeight() - Gdx.input.getY() > panelY && Gdx.graphics.getHeight() - Gdx.input.getY() < panelY + panelHeight ){
            scrollPos = scrollPos + amountY * 80;
            scrollPos = MathUntil.clamp(scrollPos, 0, maxScroll);
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


}
