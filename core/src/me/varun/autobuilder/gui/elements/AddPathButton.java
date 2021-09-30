package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.Gui;
import me.varun.autobuilder.gui.TrajectoryItem;
import org.jetbrains.annotations.NotNull;

public class AddPathButton extends AbstractGuiButton{

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;
    private final @NotNull InputEventThrower eventThrower;
    private final CameraHandler cameraHandler;

    public AddPathButton(int x, int y, int width, int height, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                         @NotNull InputEventThrower eventThrower, CameraHandler cameraHandler) {
        super(x, y, width, height, new Texture(Gdx.files.internal("path_icon.png"), true));
        this.fontShader = fontShader;
        this.font = font;
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;
    }

    @Override
    public boolean checkClick(@NotNull Gui gui) {
        if(super.checkClick(gui)){
            gui.guiItems.add(new TrajectoryItem(gui, fontShader, font, eventThrower, cameraHandler));
            UndoHandler.getInstance().somethingChanged();
            return true;
        }

        return false;

    }
}
