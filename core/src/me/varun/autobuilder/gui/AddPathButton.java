package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddPathButton extends AbstractGuiButton{

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;

    public AddPathButton(int x, int y, int width, int height, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font) {
        super(x, y, width, height, new Texture(Gdx.files.internal("path_icon.png"), true));
        this.fontShader = fontShader;
        this.font = font;
    }

    @Override
    public boolean checkClick(@NotNull Gui gui) {
        if(super.checkClick(gui)){
            gui.guiItems.add(new TrajectoryItem(gui, fontShader, font));
            return true;
        }

        return false;

    }
}
