package com.dacubeking.autobuilder.gui.gui.shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.CameraHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.events.input.NumberTextboxChangeListener;
import com.dacubeking.autobuilder.gui.gui.elements.CheckBox;
import com.dacubeking.autobuilder.gui.gui.elements.NumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.PositionedNumberTextBox;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.ScrollableGui;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.net.NetworkTablesHelper;
import com.dacubeking.autobuilder.gui.serialization.shooter.ShooterPreset;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import static com.dacubeking.autobuilder.gui.gui.GuiConstants.BUTTON_SIZE;
import static com.dacubeking.autobuilder.gui.util.Colors.LIGHT_GREY;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.*;

public class ShooterGui extends ScrollableGui implements NumberTextboxChangeListener {

    private final Viewport hudViewport;
    private final CameraHandler cameraHandler;

    private final Object drawingLoadingSymbolRenderingKey = new Object();


    ArrayList<NumberTextBox> textBoxes = new ArrayList<>();
    private static final @NotNull Color LIGHT_BLUE = Color.valueOf("5cccff");
    private long nextNetworkTablesPush = 0;
    private int lastUpdateId = 0;
    private final CheckBox checkBox;
    boolean limelightForceOn = false;
    private final TextBlock shooterConfigText = new TextBlock(Fonts.ROBOTO, 40,
            new TextComponent("Shooter Config").setColor(Color.BLACK).setBold(true));
    private final TextBlock limelightLedOnText = new TextBlock(Fonts.ROBOTO, 25,
            new TextComponent("Limelight LED On"));
    private final TextBlock distanceHoverText = new TextBlock(Fonts.ROBOTO, 13,
            new TextComponent("Distance (inches)"));
    private final TextBlock flywheelSpeedHoverText = new TextBlock(Fonts.ROBOTO, 13,
            new TextComponent("Flywheel Speed (RPM)"));
    private final TextBlock hoodEjectAngleHoverText = new TextBlock(Fonts.ROBOTO, 13,
            new TextComponent("Hood Eject Angle (degrees)"));

    ShooterConfig shooterConfig;

    private final NetworkTablesHelper networkTablesHelper = NetworkTablesHelper.getInstance();

    private final static Texture TRASH_TEXTURE = new Texture(Gdx.files.internal("trash.png"), true);

    static {
        TRASH_TEXTURE.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
    }

    private float heightOffset = -53;

    private final DecimalFormat df;

    {
        df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
    }

    public ShooterGui(Viewport hudViewport, CameraHandler cameraHandler) {
        this(hudViewport, cameraHandler, new ShooterConfig());
    }

    public ShooterGui(Viewport hudViewport, CameraHandler cameraHandler, @NotNull ShooterConfig shooterConfig) {
        super(new ShooterGuiOpenIcon(), AutoBuilder.getInstance().settingsGui);
        this.hudViewport = hudViewport;
        this.cameraHandler = cameraHandler;

        InputEventThrower.register(this);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.shooterConfig = shooterConfig;
        for (int i = 0; i <= shooterConfig.getShooterConfigs().size(); i++) {
            textBoxes.add(new PositionedNumberTextBox("", this, i, 0, 15));
            textBoxes.add(new PositionedNumberTextBox("", this, i, 1, 15));
            textBoxes.add(new PositionedNumberTextBox("", this, i, 2, 15));
        }

        networkTablesHelper.setShooterConfig(shooterConfig);
        updateSortedList();

        checkBox = new CheckBox(panelX, panelY, BUTTON_SIZE, BUTTON_SIZE); //temp positions
        networkTablesHelper.setLimelightForcedOn(false);
    }

    /**
     * @return returns true if the gui is just opening
     */
    public boolean update() {
        super.update((shooterConfig.getShooterConfigs().size() + 5) * 27);
        if (panelOpen) {
            checkBox.setX(panelWidth + panelX - 69);
            checkBox.setY(
                    panelY + panelHeight + smoothScrollPos - (shooterConfig.getShooterConfigs().size() + 2) * 27 + heightOffset - 12);
            checkBox.checkHover();

            if (checkBox.checkClick()) {
                limelightForceOn = !limelightForceOn;
                networkTablesHelper.setLimelightForcedOn(limelightForceOn);
            }
        }


        if (System.currentTimeMillis() > nextNetworkTablesPush) {
            lastUpdateId = (int) networkTablesHelper.getShooterConfigStatusId();
            networkTablesHelper.setShooterConfig(shooterConfig);
            nextNetworkTablesPush = Long.MAX_VALUE;
            System.out.println("Pushed shooter config to network tables");
        } else if (nextNetworkTablesPush != Long.MAX_VALUE) {
            AutoBuilder.scheduleRendering(nextNetworkTablesPush - System.currentTimeMillis());
        }
        return panelOpen && (isMouseOver(panelX, panelY, panelWidth, panelHeight) ||
                isMouseOver(openButton.getX(), openButton.getY(), openButton.getWidth(), openButton.getHeight()));
    }

    ArrayList<ShooterPreset> sortedShooterConfigs = new ArrayList<>();

    public void render(ShapeDrawer shapeDrawer, Batch spriteBatch, Camera camera) {
        super.render(shapeDrawer, spriteBatch);
        clickedOnTextBoxThisFrame = false;
        spriteBatch.flush();

        //The popout text part
        double distance = networkTablesHelper.getDistance();
        int index = Collections.binarySearch(sortedShooterConfigs, new ShooterPreset(0, 0, distance));
        if (index < 0) { //Convert the binary search index into an actual index
            index = -(index + 1);
        }
        ShooterPreset interpolatedShooterPreset = new ShooterPreset(0, 0, 0);
        float percentIn = 0;
        if (sortedShooterConfigs.size() > 0) {
            if (sortedShooterConfigs.get(0).getDistance() >= distance) {
                interpolatedShooterPreset = sortedShooterConfigs.get(0);
                percentIn = 0;
                index = 1;
            } else if (sortedShooterConfigs.get(sortedShooterConfigs.size() - 1).getDistance() < distance) {
                interpolatedShooterPreset = sortedShooterConfigs.get(sortedShooterConfigs.size() - 1);
                percentIn = 0;
            } else {
                //One of the above 2 if statements will true if there is only 1 element in the list
                percentIn = (float) ((distance - sortedShooterConfigs.get(index - 1).getDistance()) /
                        (sortedShooterConfigs.get(index).getDistance() - sortedShooterConfigs.get(index - 1).getDistance()));
                interpolatedShooterPreset = interpolateShooterPreset(sortedShooterConfigs.get(index - 1),
                        sortedShooterConfigs.get(index), percentIn);
            }
        }
        if (panelOpen) {
            shapeDrawer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeDrawer, panelX, panelY, panelWidth, panelHeight, 5);


            FontRenderer.renderText(spriteBatch, shapeDrawer, panelX + 10, panelY + panelHeight - 35, shooterConfigText);
            spriteBatch.flush();

            Rectangle scissors = new Rectangle();
            ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(), clipBounds, scissors);
            boolean pop = ScissorStack.pushScissors(scissors);

            boolean isMouseOnPanel = isMouseOver(getMousePos(), panelX, panelY, panelWidth, panelHeight);
            for (int i = 0; i < shooterConfig.getShooterConfigs().size(); i++) {
                //Update the text of the textboxes and render them
                textBoxes.get((i * 3) + 0).setText(String.valueOf(shooterConfig.getShooterConfigs().get(i).getDistance()));
                textBoxes.get((i * 3) + 1).setText(String.valueOf(shooterConfig.getShooterConfigs().get(i).getFlywheelSpeed()));
                textBoxes.get((i * 3) + 2).setText(String.valueOf(shooterConfig.getShooterConfigs().get(i).getHoodEjectAngle()));


                renderTextBox((i * 3), 0, shapeDrawer, spriteBatch, i, distanceHoverText, isMouseOnPanel);

                renderTextBox((i * 3), 1, shapeDrawer, spriteBatch, i, flywheelSpeedHoverText, isMouseOnPanel);

                renderTextBox((i * 3), 2, shapeDrawer, spriteBatch, i, hoodEjectAngleHoverText, isMouseOnPanel);

                spriteBatch.draw(TRASH_TEXTURE, panelX + 5 + (98 * 3),
                        panelY + panelHeight + smoothScrollPos - ((i + 1) * 27) + 4 + heightOffset, 20, 20);
            }

            //Render the last row of (blank) textboxes that are used for entering new items
            textBoxes.get(shooterConfig.getShooterConfigs().size() * 3 + 0).setText("");
            textBoxes.get(shooterConfig.getShooterConfigs().size() * 3 + 1).setText("");
            textBoxes.get(shooterConfig.getShooterConfigs().size() * 3 + 2).setText("");

            renderTextBox(shooterConfig.getShooterConfigs().size() * 3, 0, shapeDrawer, spriteBatch,
                    shooterConfig.getShooterConfigs().size(), distanceHoverText, isMouseOnPanel);
            renderTextBox(shooterConfig.getShooterConfigs().size() * 3, 1, shapeDrawer, spriteBatch,
                    shooterConfig.getShooterConfigs().size(), flywheelSpeedHoverText, isMouseOnPanel);

            renderTextBox(shooterConfig.getShooterConfigs().size() * 3, 2, shapeDrawer, spriteBatch,
                    shooterConfig.getShooterConfigs().size(), hoodEjectAngleHoverText, isMouseOnPanel);


            checkBox.render(shapeDrawer, spriteBatch, limelightForceOn);

            FontRenderer.renderText(spriteBatch, shapeDrawer, panelX + 10, checkBox.getY() + 6, limelightLedOnText);

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                if (getMouseX() > panelX + 295 && getMouseX() < panelX + panelWidth &&
                        getMouseY() > panelY && getMouseY() < panelY + panelHeight) {
                    //We are clicking in the column with the trash (delete) icons
                    int indexToDelete = (int) Math.floor(
                            ((panelY + panelHeight) - ((getMouseY() - heightOffset) - smoothScrollPos)) / 27); // Get the index of
                    // the item we want to delete
                    if (indexToDelete >= 0 && indexToDelete < shooterConfig.getShooterConfigs().size()) {
                        shooterConfig.getShooterConfigs().remove(indexToDelete);
                        for (int i = 0; i < 3; i++) { //Delete text boxes we no longer need
                            textBoxes.get(textBoxes.size() - 1).dispose();
                            textBoxes.remove(textBoxes.size() - 1);
                        }
                    }
                }
                //Sort if we're not currently editing a textbox
                if (!clickedOnTextBoxThisFrame) Collections.sort(shooterConfig.getShooterConfigs());
            }
            if (sortedShooterConfigs.size() > 0) {

                float[] vertices = {
                        //Bottom left corner
                        panelX,
                        panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27 - 14.5f + heightOffset,
                        //Top left corner
                        panelX,
                        panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27 - 10.5f + heightOffset,
                        //Top right corner
                        panelX + panelWidth + 135,
                        panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27 - 10.5f + heightOffset,
                        //Bottom right corner
                        panelX + panelWidth + 135,
                        panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27 - 48 + heightOffset,
                        //Bend 1
                        panelX + panelWidth + 20,
                        (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 48f + heightOffset,
                        //Bend 2
                        panelX + panelWidth,
                        panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27 - 14.5f + heightOffset,
                };
                LIGHT_BLUE.a = 0.8f;
                shapeDrawer.setColor(LIGHT_BLUE);
                shapeDrawer.filledPolygon(vertices);

                FontRenderer.renderText(spriteBatch, shapeDrawer,
                        panelX + panelWidth + 23,
                        panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27 - 25f + heightOffset,
                        Fonts.ROBOTO, 17,
                        //@formatter:off
                        new TextComponent(df.format(interpolatedShooterPreset.getFlywheelSpeed())).setBold(true).setColor(Color.WHITE),
                        new TextComponent(" rpm\n").setBold(false).setColor(Color.WHITE),
                        new TextComponent(df.format(interpolatedShooterPreset.getHoodEjectAngle()) +
                                ("°")).setBold(true).setColor(Color.WHITE));
                        //@formatter:on
            }

            if (pop) {
                spriteBatch.flush();
                ScissorStack.popScissors();
            }
        }

        int currId = (int) networkTablesHelper.getShooterConfigStatusId();
        int statusId = (int) networkTablesHelper.getShooterConfigStatus();
        if ((lastUpdateId == currId || statusId != 1) && networkTablesHelper.isConnected()) {
            shapeDrawer.setColor(Color.BLACK);
            shapeDrawer.arc(panelX + panelWidth - 20, panelY + 20, 10,
                    (float) -((System.currentTimeMillis() / 300d) % (Math.PI * 2)), (float) (Math.PI * 3) / 2, 4);
            AutoBuilder.enableContinuousRendering(drawingLoadingSymbolRenderingKey);
        } else {
            AutoBuilder.disableContinuousRendering(drawingLoadingSymbolRenderingKey);
        }
    }

    private void renderTextBox(int i, int xOffset, ShapeDrawer shapeDrawer, Batch spriteBatch, int yOffset,
                               TextBlock distanceHoverText, boolean isMouseOnScreen) {
        if (textBoxes.get(i + xOffset).draw(shapeDrawer, spriteBatch, panelX + 5 + (98 * xOffset),
                panelY + panelHeight + smoothScrollPos - 7 + heightOffset - yOffset * 27, 95, null)) {
            if (isMouseOnScreen) {
                HoverManager.setHoverText(distanceHoverText,
                        panelX + 5 + (98 * xOffset) + 95 / 2f,
                        panelY + panelHeight + smoothScrollPos + 2 - yOffset * 27 + heightOffset);
            }
        }
    }

    public void dispose() {
        super.dispose();
        for (NumberTextBox textBox : textBoxes) {
            textBox.dispose();
        }
        InputEventThrower.unRegister(this);
        checkBox.dispose();
        TRASH_TEXTURE.dispose();
    }

    @Override
    public void onTextChange(String text, int row, int column, NumberTextBox numberTextBox) {
        try {
            double parsedNumber = Double.parseDouble(text);
            if (row == shooterConfig.getShooterConfigs().size()) {
                //We are editing the blank row at the end and need to add a new row
                switch (column) {
                    case 0 -> shooterConfig.getShooterConfigs().add(new ShooterPreset(0, 0, parsedNumber));
                    case 1 -> shooterConfig.getShooterConfigs().add(new ShooterPreset(0, parsedNumber, 0));
                    case 2 -> shooterConfig.getShooterConfigs().add(new ShooterPreset(parsedNumber, 0, 0));
                }

                textBoxes.add(new PositionedNumberTextBox("", this, shooterConfig.getShooterConfigs().size(), 0, 15));
                textBoxes.add(new PositionedNumberTextBox("", this, shooterConfig.getShooterConfigs().size(), 1, 15));
                textBoxes.add(new PositionedNumberTextBox("", this, shooterConfig.getShooterConfigs().size(), 2, 15));
                updateSortedList();
            } else {
                switch (column) {
                    case 0 -> {
                        shooterConfig.getShooterConfigs().get(row).setDistance(parsedNumber);
                        updateSortedList();
                    }
                    case 1 -> shooterConfig.getShooterConfigs().get(row).setFlywheelSpeed(parsedNumber);
                    case 2 -> shooterConfig.getShooterConfigs().get(row).setHoodEjectAngle(parsedNumber);
                }
            }
            nextNetworkTablesPush = System.currentTimeMillis() + 500;
            AutoBuilder.scheduleRendering(500);
        } catch (NumberFormatException ignored) {

        }
    }

    @Nullable NumberTextBox lastClicked = null;
    boolean clickedOnTextBoxThisFrame;

    @Override
    public String onTextBoxClick(String text, int row, int column, NumberTextBox numberTextBox) {
        if (lastClicked != numberTextBox) {
            Collections.sort(shooterConfig.getShooterConfigs());
        }
        lastClicked = numberTextBox;
        clickedOnTextBoxThisFrame = true;
        if (row >= shooterConfig.getShooterConfigs().size()) return text;

        return switch (column) {
            case 0 -> String.valueOf(shooterConfig.getShooterConfigs().get(row).getDistance());
            case 1 -> String.valueOf(shooterConfig.getShooterConfigs().get(row).getFlywheelSpeed());
            case 2 -> String.valueOf(shooterConfig.getShooterConfigs().get(row).getHoodEjectAngle());
            default -> text;
        };
    }

    public void updateSortedList() {
        sortedShooterConfigs.clear();
        sortedShooterConfigs.addAll(shooterConfig.getShooterConfigs());
        Collections.sort(sortedShooterConfigs);
    }

    public ShooterConfig getShooterConfig() {
        return shooterConfig;
    }

    private ShooterPreset interpolateShooterPreset(ShooterPreset startValue, ShooterPreset endValue, double percentIn) {
        double flywheelSpeed =
                startValue.getFlywheelSpeed() + (endValue.getFlywheelSpeed() - startValue.getFlywheelSpeed()) * percentIn;
        double hoodPosition =
                startValue.getHoodEjectAngle() + (endValue.getHoodEjectAngle() - startValue.getHoodEjectAngle()) * percentIn;
        double distance = startValue.getDistance() + (endValue.getDistance() - startValue.getDistance()) * percentIn;

        return new ShooterPreset(hoodPosition, flywheelSpeed, distance);
    }

    public boolean isPanelOpen() {
        return panelOpen;
    }
}
