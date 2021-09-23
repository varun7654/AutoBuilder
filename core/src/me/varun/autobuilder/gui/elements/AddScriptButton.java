package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.gui.Gui;
import org.jetbrains.annotations.NotNull;

public class AddScriptButton extends  AbstractGuiButton{
    private ShaderProgram fontShader;
    private BitmapFont font;
    public AddScriptButton(int x, int y, int width, int height, ShaderProgram fontShader, BitmapFont font) {
        super(x, y, width, height, new Texture(Gdx.files.internal("script_icon.png"), true));
        this.font = font;
        this.fontShader = fontShader;
    }

    @Override
    public boolean checkClick(@NotNull Gui gui) {
        if(super.checkClick(gui)){
            return true;
        }

        return false;

    }
}
