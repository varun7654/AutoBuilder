package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.graphics.Color;

public class TextComponent {
    public String text;
    public boolean isBold = false;
    public boolean isItalic = false;
    public boolean isStrikethrough = false;
    public boolean isUnderlined = false;
    public boolean isObfuscated = false;
    public Color color = Color.WHITE;

    public TextComponent(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public TextComponent(String text) {
        this.text = text;
    }

    public TextComponent(String text, boolean isBold, boolean isItalic, boolean isStrikethrough, boolean isUnderlined,
                         boolean isObfuscated, Color color) {
        this.text = text;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isStrikethrough = isStrikethrough;
        this.isUnderlined = isUnderlined;
        this.isObfuscated = isObfuscated;
        this.color = color;
    }


}
