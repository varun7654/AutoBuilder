package me.varun.autobuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.config.Config;
import me.varun.autobuilder.events.input.InputEventThrower;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.PathGui;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.gui.textrendering.*;
import me.varun.autobuilder.net.NetworkTablesHelper;
import me.varun.autobuilder.net.Serializer;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.pathing.PointRenderer;
import me.varun.autobuilder.pathing.pointclicks.ClosePoint;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.serialization.path.GuiSerializer;
import me.varun.autobuilder.util.OsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoBuilder extends ApplicationAdapter {
    public static final float LINE_THICKNESS = 4;
    public static final float POINT_SIZE = 8;

    @NotNull private final Vector3 mousePos = new Vector3();
    @NotNull private final Vector3 lastMousePos = new Vector3();
    @NotNull OrthographicCamera cam;
    @NotNull Viewport viewport;
    @NotNull CameraHandler cameraHandler;
    @NotNull Viewport hudViewport;
    @NotNull OrthographicCamera hudCam;
    @NotNull PointRenderer origin;
    @NotNull ExecutorService pathingService = Executors.newFixedThreadPool(1);
    @NotNull PathGui pathGui;
    @NotNull InputEventThrower inputEventThrower = new InputEventThrower();
    @NotNull UndoHandler undoHandler = UndoHandler.getInstance();
    @NotNull NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();
    @NotNull private PolygonSpriteBatch batch;
    @NotNull private PolygonSpriteBatch hudBatch;
    @NotNull private Texture field;
    @NotNull private ShapeDrawer shapeRenderer;
    @NotNull private ShapeDrawer hudShapeRenderer;
    @NotNull private static Config config;
    @NotNull private Texture whiteTexture;
    @NotNull public static final String USER_DIRECTORY = OsUtil.getUserConfigDirectory("AutoBuilder");


    /**
     * Try to save data and exit the program.
     *
     * @param e The exception that caused the program to crash.
     */
    public static void handleCrash(Exception e) {
        e.printStackTrace();
        System.out.println("Something went wrong during fame " + Gdx.graphics.getFrameId());
        Gdx.app.exit();
    }

    @Override
    public void create() {
        Gdx.graphics.setForegroundFPS(Gdx.graphics.getDisplayMode().refreshRate);

        File configFile = new File(USER_DIRECTORY + "/config.json");
        configFile.getParentFile().mkdirs();
        try {
            config = (Config) Serializer.deserializeFromFile(configFile, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
            config = new Config();
        }

        //networkTables.start();

        FontHandler.updateFonts();

        Gdx.app.getInput().setInputProcessor(inputEventThrower);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.drawPixel(0, 0);
        whiteTexture = new Texture(pixmap); //remember to dispose of later
        pixmap.dispose();
        TextureRegion region = new TextureRegion(whiteTexture, 0, 0, 1, 1);

        hudBatch = new PolygonSpriteBatch();
        hudShapeRenderer = new ShapeDrawer(hudBatch, region);

        batch = new PolygonSpriteBatch();
        shapeRenderer = new ShapeDrawer(batch, region);

        field = new Texture(Gdx.files.internal("field21.png"), false);
        field.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);

        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.x = Gdx.graphics.getWidth() / 2f;
        cam.position.y = Gdx.graphics.getHeight() / 2f;
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), cam);
        cameraHandler = new CameraHandler(cam, inputEventThrower);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);



        hudCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCam.position.x = Gdx.graphics.getWidth() / 2f;
        hudCam.position.y = Gdx.graphics.getHeight() / 2f;
        hudViewport = new ScreenViewport(hudCam);

        origin = new PointRenderer(0, 0, Color.ORANGE, POINT_SIZE);

        pathGui = new PathGui(hudViewport, inputEventThrower, pathingService, cameraHandler);


        File pathFile = new File(USER_DIRECTORY +  "/" + config.getSelectedAuto());
        pathFile.getParentFile().mkdirs();

        try {
            Autonomous autonomous = Serializer.deserializeAutoFromFile(pathFile);
            undoHandler.restoreState(autonomous, pathGui, inputEventThrower, cameraHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        undoHandler.somethingChanged();
    }

    @Override
    public void render() {
        try {
            update();
            draw();
        } catch (Exception e) {
            handleCrash(e);
        }
    }

    DecimalFormat df;

    {
        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(4);
    }

    double[] frameTimes = new double[144 * 2];
    int frameTimePos = 0;


    private void draw() {
        //Clear everything
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT |
                (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        //Initialize our camera for the batch
        batch.setProjectionMatrix(cam.combined);
        shapeRenderer.setPixelSize(Math.max(cam.zoom / 8, 0.01f));

        //Draw the image
        batch.begin();
        batch.draw(field, config.getOriginX(), config.getOriginY());
        batch.flush();

        //Draw all the paths
        origin.draw(shapeRenderer, cam);
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            if (guiItem instanceof TrajectoryItem) {
                ((TrajectoryItem) guiItem).getPathRenderer().render(shapeRenderer, cam);
            }
        }


        //Draw the robot path
        for (int i = 0; i < networkTables.getRobotPositions().size() - 1; i++) {
            Float[] pos1 = networkTables.getRobotPositions().get(i);
            Float[] pos2 = networkTables.getRobotPositions().get(i + 1);
            shapeRenderer.line(pos1[0] * config.getPointScaleFactor(), pos1[1] * config.getPointScaleFactor(),
                    pos2[0] * config.getPointScaleFactor(), pos2[1] * config.getPointScaleFactor(), Color.WHITE,
                    LINE_THICKNESS);
        }

        batch.end();


        hudBatch.setProjectionMatrix(hudCam.combined);

        hudBatch.begin();
        hudBatch.setShader(null);

        //Fps overlay
        frameTimes[frameTimePos] = Gdx.graphics.getDeltaTime() * 1000;
        frameTimePos++;
        if (frameTimePos == frameTimes.length) frameTimePos = 0;
        FontRenderer.renderText(hudBatch, null, 4, 4, new TextBlock(Fonts.ROBOTO, 12,
                new TextComponent(Integer.toString(Gdx.graphics.getFramesPerSecond())).setBold(true),
                new TextComponent(" FPS, Peak: ").setBold(false),
                new TextComponent(df.format(Arrays.stream(frameTimes).max().orElseThrow())).setBold(true),
                new TextComponent(" ms, Avg: ").setBold(false),
                new TextComponent(df.format(Arrays.stream(frameTimes).average().orElseThrow())).setBold(true),
                new TextComponent(" ms").setBold(false)));

        pathGui.render(hudShapeRenderer, hudBatch, hudCam);
        hudBatch.end();

    }

    @Nullable ClosePoint lastSelectedPoint = null;
    boolean somethingMoved = false;
    private void update() {
        undoHandler.update(pathGui, inputEventThrower, cameraHandler);

        boolean onGui = pathGui.update();
        lastMousePos.set(mousePos);
        cameraHandler.update(somethingMoved, onGui);

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(mousePos);

        //Figure out the max distance a point can be from the mouse
        float maxDistance = (float) Math.pow(20 * cam.zoom, 2);

        boolean pointAdded = false;
        //Check if we need to delete/add a point
        if(Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT) || Gdx.app.getInput().isButtonJustPressed(Input.Buttons.LEFT)) {
            if (lastSelectedPoint == null || !lastSelectedPoint.parentPathRenderer.isTouchingRotationPoint(mousePos, maxDistance)) {
                removeLastSelectedPoint();

                //Get all close points and find the closest one.
                ArrayList<ClosePoint> closePoints = new ArrayList<>();
                for (AbstractGuiItem guiItem : pathGui.guiItems) {
                    if (guiItem instanceof TrajectoryItem) {
                        PathRenderer pathRenderer = ((TrajectoryItem) guiItem).getPathRenderer();
                        closePoints.addAll(pathRenderer.getClosePoints(maxDistance, mousePos));
                    }
                }
                Collections.sort(closePoints);

                //If we have a close point, select it/delete it
                if(closePoints.size() > 0) {
                    ClosePoint closestPoint = closePoints.get(0);
                    if(Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT)){
                        closestPoint.parentPathRenderer.deletePoint(closestPoint);
                        pointAdded = true;
                        somethingMoved = false;
                    } else {
                        closestPoint.parentPathRenderer.selectPoint(closestPoint, cam, mousePos, lastMousePos, pathGui.guiItems);
                        lastSelectedPoint = closestPoint;
                        somethingMoved = true;
                    }
                }
            }
        }
        //If we have a selected point, update it every frame
        if (lastSelectedPoint != null) {
            lastSelectedPoint.parentPathRenderer.updatePoint(cam, mousePos, lastMousePos);
            somethingMoved = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        }

        //Get all close points on all the trajectories and find the closest one.
        ArrayList<CloseTrajectoryPoint> closeTrajectoryPoints = new ArrayList<>();
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            if (guiItem instanceof TrajectoryItem) {
                PathRenderer pathRenderer = ((TrajectoryItem) guiItem).getPathRenderer();
                closeTrajectoryPoints.addAll(pathRenderer.getCloseTrajectoryPoints(maxDistance, mousePos));
            }
        }
        Collections.sort(closeTrajectoryPoints);

        if(closeTrajectoryPoints.size() > 0) {
            //We're hovering over a trajectory
            CloseTrajectoryPoint closeTrajectoryPoint = closeTrajectoryPoints.get(0);
            if(Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT) && !pointAdded) {
                //Should we add a point?
                closeTrajectoryPoint.parentPathRenderer.addPoint(closeTrajectoryPoint);
                removeLastSelectedPoint();
            }
            //Render the path preview
            closeTrajectoryPoint.parentPathRenderer.setRobotPathPreviewPoint(closeTrajectoryPoint);
        }

        networkTables.updateRobotPath();
    }

    public void removeLastSelectedPoint(){
        if(lastSelectedPoint != null) {
            lastSelectedPoint.parentPathRenderer.removeSelection();
            lastSelectedPoint = null;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        pathGui.dispose();
        whiteTexture.dispose();
    }


    @Override
    public void resize(int width, int height) {
        hudViewport.update(width, height, true);
        viewport.update(width, height, true);

        pathGui.updateScreen(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        File autoFile = new File(USER_DIRECTORY + "/" + config.getSelectedAuto());
        File configFile = new File(USER_DIRECTORY + "/config.json");
        autoFile.getParentFile().mkdirs();
        configFile.getParentFile().mkdirs();

        try {
            Serializer.serializeToFile(GuiSerializer.serializeAutonomous(pathGui.guiItems), autoFile);
            configFile.createNewFile();
            Serializer.serializeToFile(config, configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static @NotNull Config getConfig() {
        return config;
    }
}
