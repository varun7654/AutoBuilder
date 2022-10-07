package com.dacubeking.autobuilder.gui.gui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.dacubeking.autobuilder.gui.gui.elements.ScrollableGui;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

import static com.dacubeking.autobuilder.gui.util.MathUtil.dist2;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMousePos;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.isIsLeftMouseJustUnpressed;

public class SettingsGui extends ScrollableGui {

    private ArrayList<GuiElement> guiItems = new ArrayList<>();

    {
        guiItems.add(new GuiElement() {
            private static final TextBlock teamNumberText = new TextBlock(Fonts.ROBOTO, 20,
                    new TextComponent("Team Number: ", Color.BLACK).setBold(false));
            //private static final PathNumberTextBox teamNumberTextBox = new NumberTextBox(AutoBuilder.getConfig()
            // .getTeamNumber())

            @Override
            public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                                float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
                FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 10, drawStartY - teamNumberText.getHeight(),
                        teamNumberText);
                return teamNumberText.getHeight();
            }
        });
    }

    private float maxScroll = 0;

    public SettingsGui() {
        super(new SettingsGuiOpenIcon(), null);
    }

    public boolean update() {
        super.update(10000);
        return panelOpen;
    }

    private static final TextBlock settingsText = new TextBlock(Fonts.ROBOTO, 35,
            new TextComponent("Settings", Color.BLACK).setBold(true));

    private final Vector2 mouseDownPos = new Vector2();

    public void render(ShapeDrawer shapeDrawer, PolygonSpriteBatch batch, Camera camera) {
        super.render(shapeDrawer, batch);
        if (panelOpen) {
            shapeDrawer.setColor(Color.WHITE);
            RoundedShapeRenderer.roundedRect(shapeDrawer, panelX, panelY, panelWidth, panelHeight, 5);
            FontRenderer.renderText(batch, shapeDrawer, panelX + 5, panelY + panelHeight - settingsText.getHeight() - 5,
                    settingsText);
            Rectangle scissors = new Rectangle();

            ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
            boolean pop = ScissorStack.pushScissors(scissors);

            float yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;

            Vector2 mousePos = getMousePos();

            for (GuiElement guiItem : guiItems) {
                yPos = yPos - 10 - guiItem.render(shapeDrawer, batch, panelX + 10, yPos, panelWidth - 20,
                        camera, isIsLeftMouseJustUnpressed() && dist2(mouseDownPos, mousePos) < 10);
            }


            if (pop) {
                batch.flush();
                ScissorStack.popScissors();
            }

            maxScroll = Math.max(0, -(yPos - (int) smoothScrollPos - 10));
        }
    }
}
