package me.varun.autobuilder.gui.shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.NumberTextboxChangeListener;
import me.varun.autobuilder.gui.elements.NumberTextBox;
import me.varun.autobuilder.serialization.shooter.ShooterPreset;
import me.varun.autobuilder.util.MathUntil;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.Collections;

public class ShooterGui extends InputEventListener implements NumberTextboxChangeListener {

    private final Viewport hudViewport;
    private final BitmapFont font;
    private final ShaderProgram fontShader;
    private final InputEventThrower inputEventThrower;
    private final CameraHandler cameraHandler;
    ShooterGuiOpenIcon openIcon = new ShooterGuiOpenIcon(10, 10, 40, 40);

    ArrayList<ShooterPreset> shooterConfigs = new ArrayList<>();
    ArrayList<NumberTextBox> textBoxes = new ArrayList<>();

    protected static final @NotNull Color LIGHT_GREY = Color.valueOf("E9E9E9");

    boolean panelOpen = false;
    private float panelX;
    private float panelY;
    private float panelWidth;
    private float panelHeight;
    private @NotNull Rectangle clipBounds;
    private float scrollPos;
    private float smoothScrollPos;


    public ShooterGui(Viewport hudViewport, BitmapFont font, ShaderProgram fontShader, InputEventThrower inputEventThrower, CameraHandler cameraHandler) {

        this.hudViewport = hudViewport;
        this.font = font;
        this.fontShader = fontShader;
        this.inputEventThrower = inputEventThrower;
        this.cameraHandler = cameraHandler;

        inputEventThrower.register(this);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        textBoxes.add(new NumberTextBox("",fontShader,font, inputEventThrower, this,0,0));
        textBoxes.add(new NumberTextBox("",fontShader,font, inputEventThrower, this,0,1));
        textBoxes.add(new NumberTextBox("",fontShader,font, inputEventThrower, this,0,2));
    }

    /**
     * @return returns true if the gui is just opening
     */
    public boolean update(){
        openIcon.checkHover();
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            if(Gdx.input.getX() >= panelX && Gdx.input.getX() <= panelX + panelWidth &&
                    Gdx.graphics.getHeight() - Gdx.input.getY() >= panelY && Gdx.graphics.getHeight() - Gdx.input.getY() <= panelY + panelHeight){
                if(openIcon.checkClick()){
                    panelOpen = true;
                    scrollPos = 0;
                    smoothScrollPos = 0;
                }
            } else {
                panelOpen = false;
            }

        }

        scrollPos = MathUntil.clamp(scrollPos, -65, -65 + (shooterConfigs.size() + 1) * 27);
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos) / Math.max(1, 0.05 / Gdx.graphics.getDeltaTime()));
        return panelOpen;
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.graphics.getHeight() - Gdx.input.getY() > panelY && Gdx.graphics.getHeight() - Gdx.input.getY() < panelY + panelHeight) {
            scrollPos = scrollPos + amountY * 20;
        }
    }

    public void render(ShapeDrawer shapeDrawer, Batch spriteBatch, Camera camera) {
        clickedOnTextBoxThisFrame = false;
        openIcon.render(shapeDrawer, spriteBatch);
        spriteBatch.flush();

        if(panelOpen){
            shapeDrawer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeDrawer, panelX, panelY, panelWidth, panelHeight, 5);

            spriteBatch.setShader(fontShader);
            font.getData().setScale(0.5f);
            font.draw(spriteBatch,"Shooter Config", panelX + 10, panelY + panelHeight - 15);
            spriteBatch.setShader(null);

            Rectangle scissors = new Rectangle();
            ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(), clipBounds, scissors);
            boolean pop = ScissorStack.pushScissors(scissors);

            for (int i = 0; i < shooterConfigs.size(); i++) {
                textBoxes.get((i* 3) + 0).setText(String.valueOf(shooterConfigs.get(i).getDistance()));
                textBoxes.get((i* 3) + 1).setText(String.valueOf(shooterConfigs.get(i).getFlyWheelSpeed()));
                textBoxes.get((i* 3) + 2).setText(String.valueOf(shooterConfigs.get(i).getHoodEjectAngle()));
                textBoxes.get((i* 3) + 0).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 0),panelY + panelHeight + smoothScrollPos - i * 27,95, 25);
                textBoxes.get((i* 3) + 1).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 1),panelY + panelHeight + smoothScrollPos - i * 27,95, 25);
                textBoxes.get((i* 3) + 2).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 2),panelY + panelHeight + smoothScrollPos - i * 27 ,95, 25);
            }

            textBoxes.get(shooterConfigs.size()*3 + 0).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 0),panelY + panelHeight + smoothScrollPos - shooterConfigs.size() * 27,95, 25);
            textBoxes.get(shooterConfigs.size()*3 + 1).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 1),panelY + panelHeight + smoothScrollPos - shooterConfigs.size() * 27,95, 25);
            textBoxes.get(shooterConfigs.size()*3 + 2).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 2),panelY + panelHeight + smoothScrollPos - shooterConfigs.size() * 27,95, 25);

            if(pop){
                ScissorStack.popScissors();
            }

            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !clickedOnTextBoxThisFrame){
                Collections.sort(shooterConfigs);
            }
        }
    }

    public void updateScreen(int width, int height) {
        panelX = 10;
        panelY = 10;
        panelWidth = 300;
        panelHeight = height - 20;

        clipBounds = new Rectangle(10, panelY, panelWidth + panelX + 500,
                panelHeight- 40);
    }

    public void dispose() {
        openIcon.dispose();
        for (NumberTextBox textBox : textBoxes) {
            textBox.dispose();
        }
        inputEventThrower.unRegister(this);
    }

    @Override
    public void onTextChange(String text, int row, int column, NumberTextBox numberTextBox) {
        try{
            double parsedNumber = Double.parseDouble(text);
            if(row == shooterConfigs.size()){
                switch (column) {
                    case 0:
                        shooterConfigs.add(new ShooterPreset(0, 0, parsedNumber));
                        break;
                    case 1:
                        shooterConfigs.add(new ShooterPreset(0, parsedNumber, 0));
                        break;
                    case 2:
                        shooterConfigs.add(new ShooterPreset(parsedNumber, 0, 0));
                        break;
                }

                textBoxes.add(new NumberTextBox("", fontShader,font,inputEventThrower,this,shooterConfigs.size(),0));
                textBoxes.add(new NumberTextBox("", fontShader,font,inputEventThrower,this,shooterConfigs.size(),1));
                textBoxes.add(new NumberTextBox("", fontShader,font,inputEventThrower,this,shooterConfigs.size(),2));


            } else {
                switch (column) {
                    case 0:
                        shooterConfigs.get(row).setDistance(parsedNumber);
                        break;
                    case 1:
                        shooterConfigs.get(row).setFlywheelSpeed(parsedNumber);
                        break;
                    case 2:
                        shooterConfigs.get(row).setHoodEjectAngle(parsedNumber);
                        break;
                }
            }
        } catch (NumberFormatException ignored){

        }


   }

    @Nullable  NumberTextBox lastClicked = null;
    boolean clickedOnTextBoxThisFrame;
    @Override
    public void onTextBoxClick(String text, int row, int column, NumberTextBox numberTextBox) {
        if(lastClicked != numberTextBox){
            Collections.sort(shooterConfigs);
        }
        lastClicked = numberTextBox;
        clickedOnTextBoxThisFrame = true;
    }
}
