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
import javafx.application.Platform;
import me.varun.autobuilder.config.Config;
import me.varun.autobuilder.config.gui.ConfigGUI;
import me.varun.autobuilder.events.input.InputEventThrower;
import me.varun.autobuilder.gui.hover.HoverManager;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.PathGui;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.gui.shooter.ShooterConfig;
import me.varun.autobuilder.gui.shooter.ShooterGui;
import me.varun.autobuilder.gui.textrendering.*;
import me.varun.autobuilder.net.NetworkTablesHelper;
import me.varun.autobuilder.net.Serializer;
import me.varun.autobuilder.pathing.DrivenPathRenderer;
import me.varun.autobuilder.pathing.PointRenderer;
import me.varun.autobuilder.pathing.TrajectoryPathRenderer;
import me.varun.autobuilder.pathing.pointclicks.ClosePoint;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import me.varun.autobuilder.scripting.RobotCodeData;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.util.OsUtil;
import me.varun.autobuilder.config.gui.FileHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class AutoBuilder extends ApplicationAdapter {

    private static final AutoBuilder instance = new AutoBuilder();

    private AutoBuilder() {

    }

    public static AutoBuilder getInstance() {
        return instance;
    }

    public static final float LINE_THICKNESS = 4;
    public static final float POINT_SIZE = 8;
    public static final float CONTROL_VECTOR_SCALE = 3;
    public static final float MIN_CONTROL_VECTOR_DISTANCE = 0.1f;

    @NotNull ConfigGUI configGUI;

    @NotNull private final Vector3 mousePos = new Vector3();
    @NotNull private final Vector3 lastMousePos = new Vector3();
    @NotNull OrthographicCamera cam;
    @NotNull Viewport viewport;
    @NotNull CameraHandler cameraHandler;
    @NotNull Viewport hudViewport;
    @NotNull OrthographicCamera hudCam;
    @NotNull PointRenderer origin;
    @NotNull public static final ExecutorService asyncPathingService = Executors.newFixedThreadPool(1);
    @NotNull public static final ExecutorService asyncParsingService = Executors.newFixedThreadPool(1);
    @NotNull public PathGui pathGui;
    @NotNull public ShooterGui shooterGui;
    @NotNull InputEventThrower inputEventThrower = new InputEventThrower();
    @NotNull public UndoHandler undoHandler = UndoHandler.getInstance();
    @NotNull NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();
    @NotNull private PolygonSpriteBatch batch;
    @NotNull private PolygonSpriteBatch hudBatch;
    @NotNull private Texture field;
    @NotNull private ShapeDrawer shapeRenderer;
    @NotNull private ShapeDrawer hudShapeRenderer;
    @NotNull private static Config config = new Config();
    @NotNull private Texture whiteTexture;
    @NotNull public static final String USER_DIRECTORY = OsUtil.getUserConfigDirectory("AutoBuilder");

    @NotNull public DrivenPathRenderer drivenPathRenderer;

    @NotNull NotificationHandler notificationHandler = new NotificationHandler();


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
        Platform.startup(() -> {});
        Gdx.graphics.setForegroundFPS(Gdx.graphics.getDisplayMode().refreshRate);
        Gdx.graphics.setVSync(false);
        FileHandler.loadConfig();

        if (config.isNetworkTablesEnabled()) networkTables.start();

        FontHandler.updateFonts();
        RobotCodeData.initData();

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

        field = new Texture(Gdx.files.internal("field22.png"), false);
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

        pathGui = new PathGui(hudViewport, inputEventThrower, asyncPathingService, cameraHandler);

        FileHandler.loadAuto();

        drivenPathRenderer = new DrivenPathRenderer();

        File shooterConfigFile = config.getShooterConfigPath();
        shooterConfigFile.getParentFile().mkdirs();

        try {
            ShooterConfig shooterConfig = (ShooterConfig) Serializer.deserializeFromFile(shooterConfigFile, ShooterConfig.class);
            shooterGui = new ShooterGui(hudViewport, inputEventThrower, cameraHandler, shooterConfig);
        } catch (IOException e) {
            e.printStackTrace();
            shooterGui = new ShooterGui(hudViewport, inputEventThrower, cameraHandler);
        }
        configGUI = new ConfigGUI();
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
        //batch.flush();

        //Draw all the paths
        origin.draw(shapeRenderer, cam);

        double pathTime = 0;
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            if (guiItem instanceof TrajectoryItem) {
                ((TrajectoryItem) guiItem).getPathRenderer().render(shapeRenderer, cam);
                if (((TrajectoryItem) guiItem).getPathRenderer().getTrajectory() != null) {
                    pathTime += ((TrajectoryItem) guiItem).getPathRenderer().getTrajectory().getTotalTimeSeconds();
                }
            }
        }


        //Draw the robot path
        drivenPathRenderer.render(shapeRenderer, cam);

        batch.end();


        hudBatch.setProjectionMatrix(hudCam.combined);

        hudBatch.begin();

        TextBlock timeText = new TextBlock(Fonts.ROBOTO, 18, new TextComponent("Total Driving Time: " +
                df.format(pathTime) + "s", Color.WHITE).setBold(true));

        FontRenderer.renderText(hudBatch, shapeRenderer, Gdx.graphics.getWidth() - timeText.getWidth() - 420,
                60, timeText);

        String lastSave;
        long saveTimeDiff = System.currentTimeMillis() - FileHandler.lastSaveTime;
        if (FileHandler.lastSaveTime == -1) {
            lastSave = "Never";
        } else if (saveTimeDiff < 1000) {
            lastSave = "Just now";
        } else if (saveTimeDiff < 60000) {
            lastSave = saveTimeDiff / 1000 + "s ago";
        } else if (saveTimeDiff < 3600000) {
            lastSave = saveTimeDiff / 60000 + "m ago";
        } else {
            lastSave = saveTimeDiff / 3600000 + "h ago";
        }

        //Fps overlay
        frameTimes[frameTimePos] = Gdx.graphics.getDeltaTime() * 1000;
        frameTimePos++;
        if (frameTimePos == frameTimes.length) frameTimePos = 0;
        FontRenderer.renderText(hudBatch, null, 4, 4, new TextBlock(Fonts.JETBRAINS_MONO, 12,
                new TextComponent(Integer.toString(Gdx.graphics.getFramesPerSecond())).setBold(true).setColor(Color.WHITE),
                new TextComponent(" FPS, Peak: ").setBold(false).setColor(Color.WHITE),
                new TextComponent(df.format(Arrays.stream(frameTimes).max().orElseThrow())).setBold(true).setColor(Color.WHITE),
                new TextComponent(" ms, Avg: ").setBold(false).setColor(Color.WHITE),
                new TextComponent(df.format(Arrays.stream(frameTimes).average().orElseThrow())).setBold(true)
                        .setColor(Color.WHITE),
                new TextComponent(" ms Render Calls: ").setBold(false).setColor(Color.WHITE),
                new TextComponent(hudBatch.renderCalls + batch.renderCalls + "").setColor(Color.WHITE).setBold(true),
                new TextComponent(" Last Save: ").setBold(false).setColor(Color.WHITE),
                new TextComponent(lastSave).setColor(Color.WHITE).setBold(true)));

        pathGui.render(hudShapeRenderer, hudBatch, hudCam);
        shooterGui.render(hudShapeRenderer, hudBatch, hudCam);
        configGUI.draw(hudShapeRenderer, hudBatch, hudCam);
        HoverManager.render(hudBatch, hudShapeRenderer);

        notificationHandler.processNotification(hudShapeRenderer, hudBatch);

        hudBatch.end();
    }

    int lastCloseishPointSelectionIndex = 0;

    @Nullable ClosePoint lastSelectedPoint = null;
    boolean somethingMoved = false;

    private void update() {
        undoHandler.update(pathGui, inputEventThrower, cameraHandler);

        networkTables.updateNT();

        boolean onGui = pathGui.update();
        onGui = onGui | shooterGui.update();
        onGui = onGui | configGUI.update();
        lastMousePos.set(mousePos);
        cameraHandler.update(somethingMoved, onGui);

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(mousePos);

        //Figure out the max distance a point can be from the mouse
        float maxDistance = (float) Math.pow(20 * cam.zoom, 2);

        drivenPathRenderer.update();

        boolean pointAdded = false;
        //Check if we need to delete/add a point
        if (Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT) ||
                Gdx.app.getInput().isButtonJustPressed(Input.Buttons.LEFT)) {
            if (lastSelectedPoint == null ||
                    !lastSelectedPoint.parentTrajectoryPathRenderer.isTouchingSomething(mousePos, maxDistance)) {
                removeLastSelectedPoint();

                //Get all close points and find the closest one.
                List<ClosePoint> closePoints = new ArrayList<>();
                for (AbstractGuiItem guiItem : pathGui.guiItems) {
                    if (guiItem instanceof TrajectoryItem) {
                        TrajectoryPathRenderer trajectoryPathRenderer = ((TrajectoryItem) guiItem).getPathRenderer();
                        closePoints.addAll(trajectoryPathRenderer.getClosePoints(maxDistance, mousePos));
                    }
                }
                Collections.sort(closePoints);

                //If we have a close point, select it/delete it
                if (closePoints.size() > 0) {
                    float closestDistance = closePoints.get(0).len2 + 0.5f;
                    closePoints = closePoints.stream().filter(closePoint -> closePoint.len2 < closestDistance)
                            .collect(Collectors.toList());

                    lastCloseishPointSelectionIndex++;
                    if (lastCloseishPointSelectionIndex >= closePoints.size()) lastCloseishPointSelectionIndex = 0;

                    ClosePoint closestPoint = closePoints.get(lastCloseishPointSelectionIndex);
                    if (closestPoint.parentTrajectoryPathRenderer instanceof TrajectoryPathRenderer) {
                        TrajectoryPathRenderer trajectoryPathRenderer = (TrajectoryPathRenderer) closestPoint.parentTrajectoryPathRenderer;
                        if (Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT)) {
                            trajectoryPathRenderer.deletePoint(closestPoint);
                            pointAdded = true;
                            somethingMoved = false;
                        } else {
                            trajectoryPathRenderer.selectPoint(closestPoint, cam, mousePos, lastMousePos, pathGui.guiItems);
                            lastSelectedPoint = closestPoint;
                            somethingMoved = true;
                        }
                    }

                }

            }
        }
        //If we have a selected point, update it every frame
        if (lastSelectedPoint != null) {
            lastSelectedPoint.parentTrajectoryPathRenderer.updatePoint(cam, mousePos, lastMousePos);
            somethingMoved = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        }

        //Get all close points on all the trajectories and find the closest one.
        ArrayList<CloseTrajectoryPoint> closeTrajectoryPoints = new ArrayList<>();
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            if (guiItem instanceof TrajectoryItem) {
                TrajectoryPathRenderer trajectoryPathRenderer = ((TrajectoryItem) guiItem).getPathRenderer();
                closeTrajectoryPoints.addAll(trajectoryPathRenderer.getCloseTrajectoryPoints(maxDistance, mousePos));
            }
        }
        closeTrajectoryPoints.addAll(drivenPathRenderer.getCloseTrajectoryPoints(maxDistance, mousePos));
        Collections.sort(closeTrajectoryPoints);

        if(closeTrajectoryPoints.size() > 0) {
            //We're hovering over a trajectory
            CloseTrajectoryPoint closeTrajectoryPoint = closeTrajectoryPoints.get(0);
            if (Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT) && !pointAdded &&
                    closeTrajectoryPoint.parentTrajectoryPathRenderer instanceof TrajectoryPathRenderer) {
                //Should we add a point?
                ((TrajectoryPathRenderer) closeTrajectoryPoint.parentTrajectoryPathRenderer).addPoint(closeTrajectoryPoint);
                removeLastSelectedPoint();
            }
            //Render the path preview
            if (lastSelectedPoint == null) {
                closeTrajectoryPoint.parentTrajectoryPathRenderer.setRobotPathPreviewPoint(closeTrajectoryPoint);
            }
        }
    }

    public void removeLastSelectedPoint() {
        if (lastSelectedPoint != null && lastSelectedPoint.parentTrajectoryPathRenderer instanceof TrajectoryPathRenderer) {
            ((TrajectoryPathRenderer) lastSelectedPoint.parentTrajectoryPathRenderer).removeSelection();
            lastSelectedPoint = null;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        pathGui.dispose();
        whiteTexture.dispose();
        FontHandler.dispose();

        shooterGui.dispose();
        System.exit(0);
    }


    @Override
    public void resize(int width, int height) {
        if (width == 0 || height == 0) return;
        hudViewport.update(width, height, true);
        viewport.update(width, height, false);

        pathGui.updateScreen(width, height);
        shooterGui.updateScreen(width, height);
        configGUI.updateScreen(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        FileHandler.save();
    }

    public static @NotNull Config getConfig() {
        return config;
    }

    /**
     * Called when a file is dragged onto the window
     *
     * @param file the filepath of the file
     */
    public void loadFile(String file) {
        System.out.println("Loading file: " + file);
        FileHandler.handleFile(new File(file));
    }

    public void restoreState(Autonomous autonomous) {
        restoreState(autonomous, true);
    }

    public void restoreState(Autonomous autonomous, boolean clearUndoHistory) {
        undoHandler.restoreState(autonomous, pathGui, inputEventThrower, cameraHandler);
        if (clearUndoHistory) {
            undoHandler.clearUndoHistory();
            undoHandler.somethingChanged();
        }
    }
}
