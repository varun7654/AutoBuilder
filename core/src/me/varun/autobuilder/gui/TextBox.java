package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

public class TextBox extends InputEventListener {
    String text;
    boolean selected = false;
    ShaderProgram fontRenderer;
    BitmapFont font;

    public TextBox(String text, ShaderProgram fontRenderer, BitmapFont font){
        this.text = text;
        this.fontRenderer = fontRenderer;
        this.font = font;
    }

    public void draw(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth, int drawHeight){
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.roundedRect(drawStartX, drawStartY, drawWidth, drawHeight, 2 );
        shapeRenderer.flush();
        font.getData().setScale((drawHeight-4)/64f);
        font.draw(spriteBatch, text, drawStartX+2, drawStartY+2);
    }




}
