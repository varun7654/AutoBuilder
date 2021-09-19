package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.events.scroll.MouseScrollEventHandler;
import me.varun.autobuilder.events.scroll.MouseScrollEventThrower;
import me.varun.autobuilder.util.RoundedShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Gui implements MouseScrollEventHandler {
    private final Viewport viewport;
    private final BitmapFont font;
    private final ShaderProgram fontShader;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    private float smoothScrollPos = 1;
    private float scrollPos = 1;

    private Rectangle clipBounds;

    private List<GuiItem> guiItems = new ArrayList<>();

    public Gui(Viewport viewport, BitmapFont font, ShaderProgram fontShader, MouseScrollEventThrower eventThrower){
        this.viewport = viewport;
        this.font = font;
        this.fontShader = fontShader;

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiItems.add(new TrajectoryItem());
        guiItems.add(new TrajectoryItem());
        guiItems.add(new TrajectoryItem());

        eventThrower.register(this);


    }

    public void render(RoundedShapeRenderer shapeRenderer, Camera camera) {

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.roundedRect(panelX, panelY, panelWidth, panelHeight, 5);

        Rectangle scissors = new Rectangle();

        ScissorStack.calculateScissors(camera, shapeRenderer.getTransformMatrix(), clipBounds, scissors );
        boolean pop = ScissorStack.pushScissors(scissors);

        int yPos = Gdx.graphics.getHeight() + (int) smoothScrollPos;

        for (GuiItem guiItem : guiItems) {
            yPos = yPos- 10 - guiItem.render(shapeRenderer, panelX + 10 , yPos, panelWidth - 20, clipBounds);
        }
        shapeRenderer.flush();

        if (pop) {
            ScissorStack.popScissors();
        }
    }

    public boolean update(){
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos)/Math.max(1,0.05/Gdx.graphics.getDeltaTime()));

        //System.out.println(smoothScrollPos + " scrollpos: " + scrollPos);
        if(Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.input.getY() > panelY && Gdx.input.getY() < panelY + panelHeight ){
            return true;
        }

        return false;
    }

    public void updateScreen(int width, int height){
        panelX = (width-410);
        panelY = 10;
        panelWidth = 400;
        panelHeight = height - 20;

        clipBounds = new Rectangle(panelX, panelY, panelWidth, panelHeight);
    }

    public void dispose() {
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if(Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.input.getY() > panelY && Gdx.input.getY() < panelY + panelHeight ){
            scrollPos = scrollPos + amountY * 80;
            System.out.println(scrollPos);
        }
    }
}
