package me.varun.autobuilder.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.gui.textrendering.FontRenderer;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.util.NTUtil;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;

public class HudElement {

    Color color;
    NetworkTableEntry entry;
    String label;

    float width;

    DecimalFormat decimalFormat;

    public HudElement(NetworkTableEntry entry, String label, Color color, float width, DecimalFormat decimalFormat) {
        this.entry = entry;
        this.label = label;
        this.color = color;
        this.width = width;
        this.decimalFormat = decimalFormat;
    }


    protected void render(ShapeDrawer shapeDrawer, Batch batch, float hudXOffset, float hudYOffset) {
        String text = NTUtil.getEntryAsString(entry, decimalFormat);
        Color txColor = color;
        txColor.a = 0.95f;
        RoundedShapeRenderer.roundedRect(shapeDrawer, hudXOffset, hudYOffset, width, 30, 3, txColor);

        TextBlock textBlock = new TextBlock(Fonts.ROBOTO, 20,
                new TextComponent(label + ": ").setBold(false).setColor(Color.WHITE),
                new TextComponent(text).setBold(true).setColor(Color.WHITE).setFont(Fonts.JETBRAINS_MONO));

        if (textBlock.getWidth() > width - 8) {
            width = textBlock.getWidth() + 8;
        }

        FontRenderer.renderText(batch, shapeDrawer, hudXOffset + 4, hudYOffset + 8, textBlock);
    }

    public static HudElement fromString(String str) {
        String[] split = str.split(",");
        return new HudElement(
                NetworkTableInstance.getDefault().getEntry(split[0]),
                split[1],
                Color.valueOf(split[2]),
                Float.parseFloat(split[3]),
                new DecimalFormat(split[5])
        );
    }
}
