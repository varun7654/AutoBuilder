package me.varun.autobuilder.gui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SettingsGui {

    SettingsIcon settingsIcon = new SettingsIcon(50,10,30,30);
    boolean panelOpen = false;
    private float scrollPos;
    private float smoothScrollPos;
    private float panelX;
    private float panelY;
    private float panelWidth;
    private float panelHeight;
    private Rectangle clipBounds;
    private final Viewport hudViewport;
    private final BitmapFont font;
    private final ShaderProgram fontShader;
    private final InputEventThrower inputEventThrower;
    private final CameraHandler cameraHandler;

    public SettingsGui(Viewport hudViewport, BitmapFont font, ShaderProgram fontShader, InputEventThrower inputEventThrower, CameraHandler cameraHandler){
        this.hudViewport = hudViewport;
        this.font = font;
        this.fontShader = fontShader;
        this.inputEventThrower = inputEventThrower;
        this.cameraHandler = cameraHandler;
        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    }

    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, @NotNull Camera camera, boolean otherGuiOpen) {
        if(!otherGuiOpen) settingsIcon.render(shapeRenderer,spriteBatch);
    }

    public boolean update(boolean otherGuiOpen){
        if(otherGuiOpen){
            panelOpen = false;
            return false;
        }

        settingsIcon.checkHover();
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            if(settingsIcon.checkClick() && !panelOpen){
                panelOpen = true;
                scrollPos = 0;
                smoothScrollPos = 0;
            }

            if(!(Gdx.input.getX() >= panelX && Gdx.input.getX() <= panelX + panelWidth &&
                    Gdx.graphics.getHeight() - Gdx.input.getY() >= panelY && Gdx.graphics.getHeight() - Gdx.input.getY() <= panelY + panelHeight)) {
                //We clicked outside the panel
                panelOpen = false;
            }
        }

        //scrollPos = MathUtil.clamp(scrollPos, -65, -65 + (shooterConfig.getShooterConfigs().size() + 1) * 27);
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos) / Math.max(1, 0.05 / Gdx.graphics.getDeltaTime()));

        //panelOpen = false;
        return panelOpen;

    }


    public void updateScreen(int width, int height) {
        panelX = 10;
        panelY = 10;
        panelWidth = 325;
        panelHeight = height - 20;

        clipBounds = new Rectangle(10, panelY, panelWidth + panelX + 500,
                panelHeight- 40);
    }
}
