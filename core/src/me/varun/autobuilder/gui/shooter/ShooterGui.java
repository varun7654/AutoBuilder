package me.varun.autobuilder.gui.shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.NumberTextboxChangeListener;
import me.varun.autobuilder.gui.elements.CheckBox;
import me.varun.autobuilder.gui.elements.NumberTextBox;
import me.varun.autobuilder.net.NetworkTablesHelper;
import me.varun.autobuilder.serialization.shooter.ShooterPreset;
import me.varun.autobuilder.util.MathUtil;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class ShooterGui extends InputEventListener implements NumberTextboxChangeListener {

    private final Viewport hudViewport;
    private final BitmapFont font;
    private final ShaderProgram fontShader;
    private final InputEventThrower inputEventThrower;
    private final CameraHandler cameraHandler;
    ShooterGuiOpenIcon openIcon = new ShooterGuiOpenIcon(10, 10, 30, 30);


    ArrayList<NumberTextBox> textBoxes = new ArrayList<>();

    protected static final @NotNull Color LIGHT_GREY = Color.valueOf("E9E9E9");
    private static final @NotNull Color LIGHT_BLUE = Color.valueOf("5cccff");

    boolean panelOpen = false;
    private float panelX;
    private float panelY;
    private float panelWidth;
    private float panelHeight;
    private @NotNull Rectangle clipBounds;
    private float scrollPos;
    private float smoothScrollPos;
    private long nextNetworkTablesPush = 0;
    private int lastUpdateId = 0;
    private CheckBox checkBox;
    boolean limelightForceOn = false;

    ShooterConfig shooterConfig;

    private final NetworkTablesHelper networkTablesHelper = NetworkTablesHelper.getInstance();

    private final static Texture TRASH_TEXTURE = new Texture(Gdx.files.internal("trash.png"), true);
    static {
        TRASH_TEXTURE.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
    }

    private final DecimalFormat df;
    {
        df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
    }


    public ShooterGui(Viewport hudViewport, BitmapFont font, ShaderProgram fontShader, InputEventThrower inputEventThrower, CameraHandler cameraHandler) {
        this(hudViewport, font, fontShader, inputEventThrower, cameraHandler, new ShooterConfig());
    }

    public ShooterGui(Viewport hudViewport, BitmapFont font, ShaderProgram fontShader, InputEventThrower inputEventThrower,
                      CameraHandler cameraHandler, ShooterConfig shooterConfig) {

        this.hudViewport = hudViewport;
        this.font = font;
        this.fontShader = fontShader;
        this.inputEventThrower = inputEventThrower;
        this.cameraHandler = cameraHandler;

        inputEventThrower.register(this);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.shooterConfig = shooterConfig;
        for (int i = 0; i <= shooterConfig.getShooterConfigs().size(); i++) {
            textBoxes.add(new NumberTextBox("",fontShader,font, inputEventThrower, this, i,0));
            textBoxes.add(new NumberTextBox("",fontShader,font, inputEventThrower, this, i,1));
            textBoxes.add(new NumberTextBox("",fontShader,font, inputEventThrower, this, i,2));
        }

        networkTablesHelper.setShooterConfig(shooterConfig);
        updateSortedList();

        checkBox = new CheckBox(panelX, panelY, 30, 30); //temp positions
        networkTablesHelper.setLimelightForcedOn(false);
    }

    /**
     * @return returns true if the gui is just opening
     */
    public boolean update(boolean otherGuiOpen){
        if(otherGuiOpen){
            panelOpen = false;
            return false;
        }

        openIcon.checkHover();
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            if(openIcon.checkClick() && !panelOpen){
                panelOpen = true;
                scrollPos = 0;
                smoothScrollPos = 0;
            }

            if(!(Gdx.input.getX() >= panelX && Gdx.input.getX() <= panelX + panelWidth &&
                    Gdx.graphics.getHeight() - Gdx.input.getY() >= panelY && Gdx.graphics.getHeight() - Gdx.input.getY() <= panelY + panelHeight)) {
                //We clicked outside the panel
                panelOpen = false;
            }
        }

        scrollPos = MathUtil.clamp(scrollPos, -65, -65 + (shooterConfig.getShooterConfigs().size() + 1) * 27);
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos) / Math.max(1, 0.05 / Gdx.graphics.getDeltaTime()));

        if(panelOpen){
            checkBox.setX(panelWidth + panelX - 59);
            checkBox.setY(-3 + panelY + panelHeight + smoothScrollPos - (shooterConfig.getShooterConfigs().size() + 2) * 27);
            checkBox.checkHover();

            if(checkBox.checkClick()){
                limelightForceOn = !limelightForceOn;
                networkTablesHelper.setLimelightForcedOn(limelightForceOn);
            }
        }


        if(System.currentTimeMillis() > nextNetworkTablesPush){
            lastUpdateId = (int) networkTablesHelper.getShooterConfigStatusId();
            networkTablesHelper.setShooterConfig(shooterConfig);
            nextNetworkTablesPush = Long.MAX_VALUE;

        }
        return panelOpen;
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.graphics.getHeight() - Gdx.input.getY() > panelY && Gdx.graphics.getHeight() - Gdx.input.getY() < panelY + panelHeight) {
            scrollPos = scrollPos + amountY * 20;
        }
    }

    ArrayList<ShooterPreset> sortedShooterConfigs = new ArrayList<>();

    public void render(ShapeDrawer shapeDrawer, Batch spriteBatch, Camera camera) {
        clickedOnTextBoxThisFrame = false;
        openIcon.render(shapeDrawer, spriteBatch);
        spriteBatch.flush();

        float hudXOffset;
        float hudYOffset;

        //The popout text part
        double distance = networkTablesHelper.getDistance();
        int index = Collections.binarySearch(sortedShooterConfigs, new ShooterPreset(0,0, distance));
        if(index < 0 ){ //Convert the binary search index into an actual index
            index = -(index + 1);
        }
        ShooterPreset interpolatedShooterPreset = new ShooterPreset(0, 0,0);
        double percentIn = 0;
        if(sortedShooterConfigs.size() > 0) {
            if (sortedShooterConfigs.get(0).getDistance() >= distance) {
                interpolatedShooterPreset = sortedShooterConfigs.get(0);
                percentIn = 0;
                index = 1;
            } else if (sortedShooterConfigs.get(sortedShooterConfigs.size() - 1).getDistance() < distance) {
                interpolatedShooterPreset = sortedShooterConfigs.get(sortedShooterConfigs.size() - 1);
                percentIn = 0;
            } else {
                //One of the above 2 if statements will true if there is only 1 element in the list
                percentIn = (distance - sortedShooterConfigs.get(index - 1).getDistance()) /
                        (sortedShooterConfigs.get(index).getDistance() - sortedShooterConfigs.get(index - 1).getDistance());
                interpolatedShooterPreset = interpolateShooterPreset(sortedShooterConfigs.get(index - 1), sortedShooterConfigs.get(index), percentIn);
            }
        }
        if(panelOpen){
            shapeDrawer.setColor(LIGHT_GREY);
            RoundedShapeRenderer.roundedRect(shapeDrawer, panelX, panelY, panelWidth, panelHeight, 5);

            spriteBatch.setShader(fontShader);
            font.getData().setScale(0.5f);
            font.draw(spriteBatch,"Shooter Config", panelX + 10, panelY + panelHeight - 15);
            spriteBatch.setShader(null);

            Rectangle scissors = new Rectangle();
            ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(), clipBounds, scissors);
            boolean pop = ScissorStack.pushScissors(scissors);
            font.setColor(Color.BLACK);
            for (int i = 0; i < shooterConfig.getShooterConfigs().size(); i++) {
                //Update the text of the textboxes and render them
                textBoxes.get((i* 3) + 0).setText(String.valueOf(shooterConfig.getShooterConfigs().get(i).getDistance()));
                textBoxes.get((i* 3) + 1).setText(String.valueOf(shooterConfig.getShooterConfigs().get(i).getFlywheelSpeed()));
                textBoxes.get((i* 3) + 2).setText(String.valueOf(shooterConfig.getShooterConfigs().get(i).getHoodEjectAngle()));
                textBoxes.get((i* 3) + 0).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 0),panelY + panelHeight + smoothScrollPos - i * 27,95, 25);
                textBoxes.get((i* 3) + 1).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 1),panelY + panelHeight + smoothScrollPos - i * 27,95, 25);
                textBoxes.get((i* 3) + 2).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 2),panelY + panelHeight + smoothScrollPos - i * 27 ,95, 25);
                spriteBatch.draw(TRASH_TEXTURE,panelX + 5 +  (98 * 3),panelY + panelHeight + smoothScrollPos - ((i + 1) * 27) + 4, 20, 20);
            }

            //Render the last row of (blank) textboxes that are used for entering new items
            textBoxes.get(shooterConfig.getShooterConfigs().size()*3 + 0).setText("");
            textBoxes.get(shooterConfig.getShooterConfigs().size()*3 + 1).setText("");
            textBoxes.get(shooterConfig.getShooterConfigs().size()*3 + 2).setText("");
            textBoxes.get(shooterConfig.getShooterConfigs().size()*3 + 0).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 0),panelY + panelHeight + smoothScrollPos - shooterConfig.getShooterConfigs().size() * 27,95, 25);
            textBoxes.get(shooterConfig.getShooterConfigs().size()*3 + 1).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 1),panelY + panelHeight + smoothScrollPos - shooterConfig.getShooterConfigs().size() * 27,95, 25);
            textBoxes.get(shooterConfig.getShooterConfigs().size()*3 + 2).draw(shapeDrawer,spriteBatch,panelX + 5 +  (98 * 2),panelY + panelHeight + smoothScrollPos - shooterConfig.getShooterConfigs().size() * 27,95, 25);

            checkBox.render(shapeDrawer,spriteBatch, limelightForceOn);
            spriteBatch.setShader(fontShader);
            font.getData().setScale(0.4f);
            font.draw(spriteBatch,"Limelight LED On", panelX  + 10, checkBox.getY() + 24);

            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
                if (Gdx.input.getX() > panelX + 295 && Gdx.input.getX() < panelX + panelWidth &&
                        Gdx.graphics.getHeight() - Gdx.input.getY() > panelY && Gdx.graphics.getHeight() - Gdx.input.getY() < panelY + panelHeight) {
                    //We are clicking in the column with the trash (delete) icons
                    int indexToDelete = (int) Math.floor(((panelY + panelHeight) - ((Gdx.graphics.getHeight() - Gdx.input.getY())-smoothScrollPos))/27); // Get the index of the item we want to delete
                    if(indexToDelete>= 0 && indexToDelete < shooterConfig.getShooterConfigs().size()){
                        shooterConfig.getShooterConfigs().remove(indexToDelete);
                        for (int i = 0; i < 3; i++) { //Delete text boxes we no longer need
                            textBoxes.remove(textBoxes.size()-1);
                        }
                    }
                }
                if(!clickedOnTextBoxThisFrame) Collections.sort(shooterConfig.getShooterConfigs());
            }
            if(sortedShooterConfigs.size() >0){

                float[] vertices = {
                        panelX, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 14.5f, //Bottom left corner
                        panelX, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 10.5f, //Top left corner
                        panelX + panelWidth + 135, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 10.5f, //Top right corner
                        panelX + panelWidth + 135, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 55, //Bottom right corner
                        panelX + panelWidth + 20, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 55f, //Bend 1
                        panelX + panelWidth, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 14.5f, //Bend 2
                };
                LIGHT_BLUE.a = 0.8f;
                shapeDrawer.setColor(LIGHT_BLUE);
                shapeDrawer.filledPolygon(vertices);
                //shapeDrawer.filledRectangle(panelX, (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 12.5f, panelWidth+100, 4);

                spriteBatch.setShader(fontShader);
                font.getData().setScale(0.3f);
                font.setColor(Color.WHITE);
                font.draw(spriteBatch,df.format(interpolatedShooterPreset.getFlywheelSpeed()) + " rpm \n" + df.format(interpolatedShooterPreset.getHoodEjectAngle()) + "°",
                        panelX+panelWidth+20,
                        (float) (panelY + panelHeight + smoothScrollPos - (index - 1 + percentIn) * 27) - 14.5f);
                spriteBatch.setShader(null);
            }

            if(pop){
                ScissorStack.popScissors();
            }

            hudXOffset = panelX + panelWidth + 10;
            hudYOffset = panelY + panelHeight - 30;

        } else {

            hudXOffset = 10;
            hudYOffset = panelY + panelHeight - 30;
        }


        Color txColor = new Color().fromHsv((float) (Math.abs(networkTablesHelper.getLimelightHorizontalOffset()/30) * 246 + 113), 0.6f, 1f);
        txColor.a = 0.95f;
        RoundedShapeRenderer.roundedRect(shapeDrawer, hudXOffset, hudYOffset,160, 30, 3, txColor);
        spriteBatch.setShader(fontShader);
        font.getData().setScale(0.45f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "tx:" + df.format(networkTablesHelper.getLimelightHorizontalOffset()), hudXOffset+4, hudYOffset+27);

        Color tyColor = new Color().fromHsv((float) (Math.abs(networkTablesHelper.getLimelightHorizontalOffset()/30) * 246 + 113), 0.6f, 1f);
        tyColor.a = 0.95f;
        RoundedShapeRenderer.roundedRect(shapeDrawer, hudXOffset + 165, hudYOffset,160, 30, 3, txColor);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "ty:" + df.format(networkTablesHelper.getLimelightVerticalOffset()), hudXOffset+4 + 165, hudYOffset+27);

        Color distanceColor = new Color().fromHsv(113f, 0.6f, 1f);
        distanceColor.a = 0.95f;
        RoundedShapeRenderer.roundedRect(shapeDrawer, hudXOffset + 165 * 2, hudYOffset,160, 30, 3, distanceColor);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "dist:" + df.format(networkTablesHelper.getDistance()), hudXOffset+4 + 165*2, hudYOffset+27);



        Color rpmColor = new Color().fromHsv((float) (Math.abs((networkTablesHelper.getShooterRPM()- interpolatedShooterPreset.getFlywheelSpeed())/6000) * 246 + 113), 0.6f, 1f);
        rpmColor.a = 0.95f;
        RoundedShapeRenderer.roundedRect(shapeDrawer, hudXOffset, hudYOffset - 35,160, 30, 3, rpmColor);
        spriteBatch.setShader(fontShader);
        font.getData().setScale(0.45f);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "rpm:" + df.format(networkTablesHelper.getShooterRPM()), hudXOffset+4, hudYOffset+27-35);

        Color hoodAngleColor = new Color().fromHsv((float) (Math.abs((networkTablesHelper.getLimelightHorizontalOffset() - interpolatedShooterPreset.getHoodEjectAngle())/30) * 246 + 113), 0.6f, 1f);
        hoodAngleColor.a = 0.95f;
        RoundedShapeRenderer.roundedRect(shapeDrawer, hudXOffset + 165, hudYOffset -35 ,160, 30, 3, hoodAngleColor);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "hood:" + df.format(networkTablesHelper.getHoodAngle()), hudXOffset+4 + 165, hudYOffset+27 -35);

        spriteBatch.setShader(null);
        int currId = (int) networkTablesHelper.getShooterConfigStatusId();
        int statusId = (int) networkTablesHelper.getShooterConfigStatus();
        if(lastUpdateId == currId || statusId != 1){
            shapeDrawer.setColor(Color.BLACK);
            shapeDrawer.arc(panelX + panelWidth - 20, panelY + 20,10, (float) -((System.currentTimeMillis()/300d) % (Math.PI * 2)), (float) (Math.PI*3)/2, 4);
        }
    }


    public void updateScreen(int width, int height) {
        panelX = 10;
        panelY = 10;
        panelWidth = 325;
        panelHeight = height - 20;

        clipBounds = new Rectangle(10, panelY, panelWidth + panelX + 500,
                panelHeight- 40);
    }

    public void dispose() {
        openIcon.dispose();
        for (NumberTextBox textBox : textBoxes) {
            textBox.dispose();
        }
        inputEventThrower.unRegister(this);
        checkBox.dispose();
        TRASH_TEXTURE.dispose();
    }

    @Override
    public void onTextChange(String text, int row, int column, NumberTextBox numberTextBox) {
        try{
            double parsedNumber = Double.parseDouble(text);
            if(row == shooterConfig.getShooterConfigs().size()){
                switch (column) {
                    case 0:
                        shooterConfig.getShooterConfigs().add(new ShooterPreset(0, 0, parsedNumber));
                        break;
                    case 1:
                        shooterConfig.getShooterConfigs().add(new ShooterPreset(0, parsedNumber, 0));
                        break;
                    case 2:
                        shooterConfig.getShooterConfigs().add(new ShooterPreset(parsedNumber, 0, 0));
                        break;
                }

                textBoxes.add(new NumberTextBox("", fontShader,font,inputEventThrower,this, shooterConfig.getShooterConfigs().size(),0));
                textBoxes.add(new NumberTextBox("", fontShader,font,inputEventThrower,this, shooterConfig.getShooterConfigs().size(),1));
                textBoxes.add(new NumberTextBox("", fontShader,font,inputEventThrower,this, shooterConfig.getShooterConfigs().size(),2));
                updateSortedList();


            } else {
                switch (column) {
                    case 0:
                        shooterConfig.getShooterConfigs().get(row).setDistance(parsedNumber);
                        updateSortedList();
                        break;
                    case 1:
                        shooterConfig.getShooterConfigs().get(row).setFlywheelSpeed(parsedNumber);
                        break;
                    case 2:
                        shooterConfig.getShooterConfigs().get(row).setHoodEjectAngle(parsedNumber);
                        break;
                }
            }
            nextNetworkTablesPush = System.currentTimeMillis() + 500;
        } catch (NumberFormatException ignored){

        }


   }

    @Nullable  NumberTextBox lastClicked = null;
    boolean clickedOnTextBoxThisFrame;
    @Override
    public String onTextBoxClick(String text, int row, int column, NumberTextBox numberTextBox) {
        if(lastClicked != numberTextBox){
            Collections.sort(shooterConfig.getShooterConfigs());
        }
        lastClicked = numberTextBox;
        clickedOnTextBoxThisFrame = true;
        if(row >= shooterConfig.getShooterConfigs().size()) return text;

        switch (column){
            case 0:
                return String.valueOf(shooterConfig.getShooterConfigs().get(row).getDistance());
            case 1:
                return String.valueOf(shooterConfig.getShooterConfigs().get(row).getFlywheelSpeed());
            case 2:
                return String.valueOf(shooterConfig.getShooterConfigs().get(row).getHoodEjectAngle());
            default:
                return text;
        }
    }

    public void updateSortedList(){
        sortedShooterConfigs.clear();
        sortedShooterConfigs.addAll(shooterConfig.getShooterConfigs());
        Collections.sort(sortedShooterConfigs);
    }

    public ShooterConfig getShooterConfig() {
        return shooterConfig;
    }

    private ShooterPreset interpolateShooterPreset(ShooterPreset startValue, ShooterPreset endValue, double percentIn) {
        double flywheelSpeed = startValue.getFlywheelSpeed() + (endValue.getFlywheelSpeed() - startValue.getFlywheelSpeed()) * percentIn;
        double hoodPosition = startValue.getHoodEjectAngle() + (endValue.getHoodEjectAngle() - startValue.getHoodEjectAngle()) * percentIn;
        double distance = startValue.getDistance() + (endValue.getDistance() - startValue.getDistance()) * percentIn;

        return new ShooterPreset(hoodPosition, flywheelSpeed, distance);
    }
}
