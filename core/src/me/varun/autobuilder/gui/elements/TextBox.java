package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
import me.varun.autobuilder.util.MathUntil;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class TextBox extends InputEventListener {
    @NotNull protected String text;
    private boolean selected = false;
    @NotNull private final ShaderProgram fontShader;
    @NotNull private final BitmapFont font;
    private long nextFlashChange = 0;
    private boolean flashing = false;
    private int selectedPos = 0;
    @NotNull private final InputEventThrower eventThrower;
    @NotNull private final GlyphLayout glyphLayout = new GlyphLayout();
    @NotNull protected final TextChangeListener textChangeListener;

    public TextBox(@NotNull String text, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                   @NotNull InputEventThrower eventThrower, @NotNull TextChangeListener textChangeListener){
        this.textChangeListener = textChangeListener;
        this.text = text;
        this.fontShader = fontShader;
        this.font = font;
        this.eventThrower = eventThrower;
        eventThrower.register(this);
    }

    public void draw(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX,
                     int drawStartY, int drawWidth, int drawHeight){
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){

            if(Gdx.input.getX() > drawStartX && Gdx.input.getX() < drawStartX + drawWidth
                    && Gdx.graphics.getHeight() - Gdx.input.getY() > drawStartY-drawHeight
                    && Gdx.graphics.getHeight() - Gdx.input.getY() < drawStartY ){
                selected = true;

                int relativeMousePos = Gdx.input.getX() - drawStartX - 4;
                System.out.println(relativeMousePos);
                selectedPos = -1;
                if(relativeMousePos < 0) {
                    selectedPos = 0;
                } else {
                    float lastTextPos = 0;
                    for (int i = 0; i < (text + " ").length(); i++) {
                        String substring = (text + " ").substring(0 , i);
                        glyphLayout.setText(font, substring);
                        System.out.println(i + ": " + glyphLayout.width);
                        if(relativeMousePos < glyphLayout.width) {
                            if(glyphLayout.width - relativeMousePos > relativeMousePos - lastTextPos){
                                selectedPos = i-1;
                            } else {
                                selectedPos = i;
                            }

                            System.out.println((glyphLayout.width - relativeMousePos) + " " +  (relativeMousePos - lastTextPos));

                            break;
                        }
                        lastTextPos = glyphLayout.width;
                    }
                    if(selectedPos == -1) {
                        if(glyphLayout.width - relativeMousePos > relativeMousePos - lastTextPos){
                            selectedPos = text.length()-1;
                        } else {
                            selectedPos = text.length();
                        }
                    };
                }
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;

            } else {
                selected = false;
            }
        }

        if(selected){
            if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
                selectedPos++;
                if(selectedPos > text.length()){
                    selectedPos = text.length();
                }
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            }

            if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
                selectedPos--;
                if(selectedPos < 0){
                    selectedPos = 0;
                }
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            }

            if(Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)){
                if(selectedPos > 0){
                    text = text.substring(0, selectedPos-1) + text.substring(selectedPos);
                    selectedPos--;
                    fireTextChangeEvent();
                }
            }

            if(nextFlashChange < Calendar.getInstance().getTimeInMillis()){
                flashing = !flashing;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            }
        }

        font.getData().setScale((drawHeight-2)/64f);

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.roundedRect(drawStartX, drawStartY-drawHeight, drawWidth, drawHeight, 2 );
        shapeRenderer.flush();

        spriteBatch.setShader(fontShader);
        font.draw(spriteBatch, text, drawStartX+4, drawStartY-4);

        if(selected  && flashing){
            glyphLayout.setText(font , text);
            glyphLayout.setText(font, text.substring(0, selectedPos));
            float cursorRenderPos = glyphLayout.width;
            font.draw(spriteBatch, "|", drawStartX + 4 + cursorRenderPos, drawStartY - 4);
        }

        spriteBatch.setShader(null);
    }

    public void dispose(){
        eventThrower.unRegister(this);
    }

    @Override
    public void onKeyType(char character) {
        if(selected){
            if (text.length() == selectedPos) {
                text = text + character;
            } else {
                text = text.substring(0, selectedPos) + character + text.substring(selectedPos);
            }
            selectedPos++;
            flashing = true;
            nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            fireTextChangeEvent();
        }
    }

    protected void fireTextChangeEvent() {

    }

    @Override
    public String toString() {
        return "TextBox{" +
                "text='" + text + '\'' +
                ", selected=" + selected +
                ", fontShader=" + fontShader +
                ", font=" + font +
                ", nextFlashChange=" + nextFlashChange +
                ", flashing=" + flashing +
                ", selectedPos=" + selectedPos +
                ", eventThrower=" + eventThrower +
                '}';
    }
}
