package com.dacubeking.autobuilder.gui.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.NTUtil;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import edu.wpi.first.networktables.NetworkTableEvent.Kind;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Topic;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.EnumSet;

public class HudElement implements Disposable {

    protected final Color color;
    protected final Topic entry;
    protected final String label;
    protected float width;
    protected final DecimalFormat decimalFormat;
    protected final int listenerHandle;

    public HudElement(Topic entry, String label, Color color, float width, DecimalFormat decimalFormat) {
        this.entry = entry;
        this.label = label;
        this.color = color;
        this.width = width;
        this.decimalFormat = decimalFormat;

        listenerHandle = NetworkTableInstance.getDefault().addListener(entry,
                EnumSet.of(Kind.kImmediate, Kind.kValueRemote), entryNotification -> AutoBuilder.requestRendering());
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

    public static @Nullable HudElement fromString(String str) {
        String[] split = str.split(",");
        if (split.length != 5) {
            return null;
        }
        return new HudElement(
                NetworkTableInstance.getDefault().getTopic(split[0]),
                split[1],
                Color.valueOf(split[2]),
                Float.parseFloat(split[3]),
                new DecimalFormat(split[4])
        );
    }

    @Override
    public void dispose() {
        NetworkTableInstance.getDefault().removeListener(listenerHandle);
    }
}
