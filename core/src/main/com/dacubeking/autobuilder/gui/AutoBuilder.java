package com.dacubeking.autobuilder.gui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dacubeking.autobuilder.gui.config.Config;
import com.dacubeking.autobuilder.gui.config.gui.ConfigGUI;
import com.dacubeking.autobuilder.gui.config.gui.FileHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.gui.drawable.DrawableRenderer;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.hud.HudRenderer;
import com.dacubeking.autobuilder.gui.gui.notification.NotificationHandler;
import com.dacubeking.autobuilder.gui.gui.path.AbstractGuiItem;
import com.dacubeking.autobuilder.gui.gui.path.PathGui;
import com.dacubeking.autobuilder.gui.gui.path.TrajectoryItem;
import com.dacubeking.autobuilder.gui.gui.settings.SettingsGui;
import com.dacubeking.autobuilder.gui.gui.shooter.ShooterConfig;
import com.dacubeking.autobuilder.gui.gui.shooter.ShooterGui;
import com.dacubeking.autobuilder.gui.gui.textrendering.*;
import com.dacubeking.autobuilder.gui.net.NetworkTablesHelper;
import com.dacubeking.autobuilder.gui.net.Serializer;
import com.dacubeking.autobuilder.gui.pathing.DrivenPathRenderer;
import com.dacubeking.autobuilder.gui.pathing.PointRenderer;
import com.dacubeking.autobuilder.gui.pathing.TrajectoryPathRenderer;
import com.dacubeking.autobuilder.gui.pathing.pointclicks.ClosePoint;
import com.dacubeking.autobuilder.gui.pathing.pointclicks.CloseTrajectoryPoint;
import com.dacubeking.autobuilder.gui.scripting.RobotCodeData;
import com.dacubeking.autobuilder.gui.serialization.path.Autonomous;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.undo.UndoState;
import com.dacubeking.autobuilder.gui.util.MouseUtil;
import com.dacubeking.autobuilder.gui.util.OsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseX;

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
    @NotNull private final Vector3 mouseDiff = new Vector3();
    @NotNull OrthographicCamera cam;
    @NotNull Viewport viewport;
    @NotNull public CameraHandler cameraHandler;
    @NotNull Viewport hudViewport;
    @NotNull OrthographicCamera hudCam;
    @NotNull PointRenderer origin;
    @NotNull public static final ExecutorService asyncPathingService = Executors.newFixedThreadPool(1);
    @NotNull public static final ExecutorService asyncParsingService = Executors.newFixedThreadPool(1);
    @NotNull public PathGui pathGui;
    @Nullable public ShooterGui shooterGui;
    @NotNull public SettingsGui settingsGui;
    @NotNull InputEventThrower inputEventThrower = new InputEventThrower();
    @NotNull public UndoHandler undoHandler = UndoHandler.getInstance();
    @NotNull NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();
    @NotNull private PolygonSpriteBatch batch;
    @NotNull private PolygonSpriteBatch hudBatch;
    @NotNull private Texture field;
    @NotNull private ShapeDrawer shapeRenderer;
    @NotNull private ShapeDrawer hudShapeRenderer;
    @NotNull private static final Config config = new Config();
    @NotNull private Texture whiteTexture;
    @NotNull public static final String USER_DIRECTORY = OsUtil.getUserConfigDirectory("AutoBuilder");
    static int fps;

    @NotNull public DrivenPathRenderer drivenPathRenderer;

    @NotNull public HudRenderer hudRenderer;
    @NotNull public DrawableRenderer drawableRenderer;

    @NotNull NotificationHandler notificationHandler = new NotificationHandler();


    /**
     * Try to save data and exit the program.
     *
     * @param e The exception that caused the program to crash.
     */
    public static void handleCrash(Throwable e) {
        e.printStackTrace();
        System.out.println("Something went wrong during fame " + Gdx.graphics.getFrameId());
        Gdx.app.exit();
    }

    @Override
    public void create() {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> handleCrash(e));
        Gdx.graphics.setForegroundFPS(Gdx.graphics.getDisplayMode().refreshRate);
        Gdx.graphics.setVSync(false);
        FileHandler.loadConfig();

        fps = Gdx.graphics.getDisplayMode().refreshRate;
        Gdx.graphics.setForegroundFPS(Gdx.graphics.getDisplayMode().refreshRate);
        Gdx.graphics.setContinuousRendering(true);
        enableContinuousRendering(this);

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

        if (new File(USER_DIRECTORY + "/field.png").exists()) {
            field = new Texture(new FileHandle(USER_DIRECTORY + "/field.png"), true);
        } else {
            field = new Texture(Gdx.files.internal("field22.png"), true);
        }

        field.setFilter(TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Nearest);

        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.x = Gdx.graphics.getWidth() / 2f;
        cam.position.y = Gdx.graphics.getHeight() / 2f;
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), cam);
        cameraHandler = new CameraHandler(cam);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);


        hudCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCam.position.x = Gdx.graphics.getWidth() / 2f;
        hudCam.position.y = Gdx.graphics.getHeight() / 2f;
        hudViewport = new ScreenViewport(hudCam);

        origin = new PointRenderer(0, 0, Color.ORANGE, POINT_SIZE);

        pathGui = new PathGui(asyncPathingService, cameraHandler);

        settingsGui = new SettingsGui();

        FileHandler.handleFile(getConfig().getAutoPath());

        drivenPathRenderer = new DrivenPathRenderer();
        hudRenderer = new HudRenderer();
        drawableRenderer = new DrawableRenderer();

        if (config.isShooterConfigEnabled()) {
            File shooterConfigFile = config.getShooterConfigPath();
            shooterConfigFile.getParentFile().mkdirs();

            try {
                ShooterConfig shooterConfig = (ShooterConfig) Serializer.deserializeFromFile(shooterConfigFile,
                        ShooterConfig.class);
                shooterGui = new ShooterGui(hudViewport, cameraHandler, shooterConfig);
            } catch (IOException e) {
                e.printStackTrace();
                shooterGui = new ShooterGui(hudViewport, cameraHandler);
            }
        }
        configGUI = new ConfigGUI();

        if (config.isNetworkTablesEnabled()) {
            networkTables.start(hudRenderer, drawableRenderer);
        }
        FileHandler.startAutoSaveThread(); // Start the autoSave thread only after we've loaded everything to prevent the auto
        // from getting autosaved (deleted) before it is loaded
    }

    long renderTimeNano = 0L;
    static Set<Object> continuousRendering = Collections.synchronizedSet(new HashSet<>());

    public static void enableContinuousRendering(Object obj) {
        if (!continuousRendering.contains(obj)) {
            continuousRendering.add(obj);
            if (!Gdx.graphics.isContinuousRendering()) {
                justStartedRendering.set(true);
                Gdx.graphics.setContinuousRendering(true);
            }
        }
    }

    private static final Runnable RENDER = () -> {};

    public static void requestRendering() {
        justStartedRendering.set(true);
        Gdx.app.postRunnable(RENDER);
    }

    public static void somethingInputted() {
        if (!Gdx.graphics.isContinuousRendering()) {
            justStartedRendering.set(true);
        }
    }

    public static void disableContinuousRendering(Object obj) {
        continuousRendering.remove(obj);
        Gdx.graphics.setContinuousRendering(continuousRendering.size() > 0);
    }

    static AtomicBoolean justStartedRendering = new AtomicBoolean();

    /**
     * Should be used instead of {@link Graphics#getDeltaTime()} to get the time since the last frame. If rendering is not
     * continuous, it will instead return the expected dt (calculated from the frame rate).
     *
     * @return the time span between the current frame and the last frame in seconds.
     */
    public static float getDeltaTime() {
        return justStartedRendering.get() ? 1f / fps : Gdx.graphics.getDeltaTime();
    }

    static ScheduledThreadPoolExecutor requestedRenderThread = new ScheduledThreadPoolExecutor(1);

    static {
        requestedRenderThread.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        requestedRenderThread.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        requestedRenderThread.setRemoveOnCancelPolicy(true);
    }

    /**
     * @param delay in milliseconds. Will only be added if a more recent request has not been made.
     */
    public synchronized static void scheduleRendering(long delay) {
        if (delay < 0) {
            return;
        }
        @Nullable ScheduledFuture<?> runnable = (ScheduledFuture<?>) requestedRenderThread.getQueue().peek();
        if (runnable == null || runnable.getDelay(TimeUnit.MILLISECONDS) > delay) {
            if (runnable != null) {
                runnable.cancel(false);
            }
            requestedRenderThread.schedule(AutoBuilder::requestRendering, delay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void render() {
        long prev = System.nanoTime();
        try {
            Gdx.graphics.setForegroundFPS(fps);
            setMouseCursor(SystemCursor.Arrow);
            if (Gdx.graphics.getFrameId() == 700) {
                disableContinuousRendering(this);
            }
            update();
            draw();
        } catch (Exception e) {
            handleCrash(e);
        }
        renderTimeNano = System.nanoTime() - prev;
    }

    DecimalFormat df;

    {
        df = new DecimalFormat();
        df.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(4);
    }

    double[] frameTimes = new double[60];
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

        drawableRenderer.render(shapeRenderer, batch);
        batch.end();


        hudBatch.setProjectionMatrix(hudCam.combined);

        hudBatch.begin();

        TextBlock timeText = new TextBlock(Fonts.ROBOTO, 18, new TextComponent("Total Driving Time: " +
                df.format(pathTime) + "s", Color.WHITE).setBold(true));

        FontRenderer.renderText(hudBatch, shapeRenderer, pathGui.getPanelX() - timeText.getWidth() - 10,
                60, timeText);

        String lastSave;
        long saveTimeDiff = System.currentTimeMillis() - FileHandler.lastSaveTime;
        if (FileHandler.saving) {
            lastSave = "Saving now";
        } else if (FileHandler.savingError) {
            lastSave = "Error saving";
        } else if (FileHandler.lastSaveTime == -1) {
            lastSave = "Never";
        } else if (saveTimeDiff < 3000) {
            lastSave = "Just now";
            scheduleRendering(3001 - saveTimeDiff);
        } else if (saveTimeDiff < 15000) {
            lastSave = "A couple of seconds ago";
            scheduleRendering(15001 - saveTimeDiff);
        } else if (saveTimeDiff < 60000) {
            lastSave = "In the last minute";
            scheduleRendering(60001 - saveTimeDiff);
        } else if (saveTimeDiff < 3600000) {
            lastSave = saveTimeDiff / 60000 + "m ago";
            scheduleRendering((60000 - (saveTimeDiff % 60000)) + 1);
        } else {
            lastSave = saveTimeDiff / 3600000 + "h ago";
            scheduleRendering((3600000 - (saveTimeDiff % 3600000)) + 1);
        }

        //Fps overlay
        frameTimes[frameTimePos] = renderTimeNano * 0.000001;
        frameTimePos++;
        if (frameTimePos == frameTimes.length) {
            frameTimePos = 0;
        }
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

        if (shooterGui != null) { //If the shooter gui is null, it means its disabled
            shooterGui.render(hudShapeRenderer, hudBatch, hudCam);
        }

        settingsGui.render(hudShapeRenderer, hudBatch, hudCam);

        configGUI.draw(hudShapeRenderer, hudBatch, hudCam);
        float hudXOffset = 0;
        if ((shooterGui != null && shooterGui.isPanelOpen()) || settingsGui.isPanelOpen()) {
            hudXOffset = 340;
        }
        hudRenderer.render(hudShapeRenderer, hudBatch, hudXOffset);
        HoverManager.render(hudBatch, hudShapeRenderer);
        notificationHandler.processNotification(hudShapeRenderer, hudBatch);
        hudBatch.end();
        justStartedRendering.set(false);

        Gdx.graphics.setSystemCursor(systemCursor);
    }

    int lastCloseishPointSelectionIndex = 0;

    @Nullable ClosePoint lastSelectedPoint = null;
    boolean somethingMoved = false;

    private void update() {
        MouseUtil.update();
        undoHandler.update(pathGui, cameraHandler);
        boolean onGui = pathGui.update();
        onGui = onGui | settingsGui.update();
        if (shooterGui != null) {
            onGui = onGui | shooterGui.update();
        }
        onGui = onGui | configGUI.update();
        lastMousePos.set(mousePos);
        cameraHandler.update(somethingMoved, onGui);

        mousePos.set(getMouseX(), Gdx.input.getY(), 0);
        cam.unproject(mousePos);
        mouseDiff.set(mousePos).sub(lastMousePos);

        if (MouseUtil.isAltPressed() && !onGui) {
            HoverManager.setHoverText(new TextBlock(Fonts.JETBRAINS_MONO, 14,
                            new TextComponent("X: ").setBold(true),
                            new TextComponent(df.format(mousePos.x / AutoBuilder.getConfig().getPointScaleFactor())),
                            new TextComponent(" Y: ").setBold(true),
                            new TextComponent(df.format(mousePos.y / AutoBuilder.getConfig().getPointScaleFactor()))),
                    0, Gdx.graphics.getHeight() - 2);
        }
        //Figure out the max distance a point can be from the mouse
        float maxDistance = (float) Math.pow(Math.max(20 * cam.zoom, POINT_SIZE), 2);

        drivenPathRenderer.update();

        boolean pointAdded = false;
        //Check if we need to delete/add a point

        if ((Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT) ||
                Gdx.app.getInput().isButtonJustPressed(Input.Buttons.LEFT)) && !onGui) {
            double distToClosestPointNotMainPoint = lastSelectedPoint == null ? Double.MAX_VALUE :
                    (lastSelectedPoint.parentTrajectoryPathRenderer().distToClosestPointNotMainPoint(mousePos));
            if (distToClosestPointNotMainPoint > maxDistance) {
                removeLastSelectedPoint();
            }

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
                if (closePoints.get(0).len2() < distToClosestPointNotMainPoint) {
                    removeLastSelectedPoint();
                    float closestDistance = closePoints.get(0).len2() + 0.5f;
                    closePoints = closePoints.stream().filter(closePoint -> closePoint.len2() < closestDistance)
                            .collect(Collectors.toList());

                    lastCloseishPointSelectionIndex++;
                    if (lastCloseishPointSelectionIndex >= closePoints.size()) {
                        lastCloseishPointSelectionIndex = 0;
                    }

                    ClosePoint closestPoint = closePoints.get(lastCloseishPointSelectionIndex);
                    if (closestPoint.parentTrajectoryPathRenderer() instanceof TrajectoryPathRenderer trajectoryPathRenderer) {
                        if (Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT)) {
                            trajectoryPathRenderer.deletePoint(closestPoint);
                            pointAdded = true;
                            somethingMoved = false;
                        } else {
                            trajectoryPathRenderer.selectPoint(closestPoint, cam, mousePos, mouseDiff,
                                    pathGui.guiItems);
                            lastSelectedPoint = closestPoint;
                            somethingMoved = true;
                        }
                    }
                }
            }
        }
        //If we have a selected point, update it every frame
        if (lastSelectedPoint != null) {
            lastSelectedPoint.parentTrajectoryPathRenderer().updatePoint(cam, mousePos, mouseDiff);
            somethingMoved = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        }

        if (!onGui) {
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

            if (closeTrajectoryPoints.size() > 0) {
                //We're hovering over a trajectory
                CloseTrajectoryPoint closeTrajectoryPoint = closeTrajectoryPoints.get(0);
                if (Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT) && !pointAdded &&
                        closeTrajectoryPoint.parentTrajectoryPathRenderer() instanceof TrajectoryPathRenderer) {
                    //Should we add a point?
                    ((TrajectoryPathRenderer) closeTrajectoryPoint.parentTrajectoryPathRenderer()).addPoint(closeTrajectoryPoint);
                    removeLastSelectedPoint();
                }
                //Render the path preview
                if (lastSelectedPoint == null) {
                    closeTrajectoryPoint.parentTrajectoryPathRenderer().setRobotPathPreviewPoint(closeTrajectoryPoint);
                }
            }
        }
    }

    public void removeLastSelectedPoint() {
        if (lastSelectedPoint != null && lastSelectedPoint.parentTrajectoryPathRenderer() instanceof TrajectoryPathRenderer) {
            ((TrajectoryPathRenderer) lastSelectedPoint.parentTrajectoryPathRenderer()).removeSelection();
            lastSelectedPoint = null;
        }
    }

    private static SystemCursor systemCursor = SystemCursor.Arrow;

    public static void setMouseCursor(SystemCursor cursor) {
        systemCursor = cursor;
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        pathGui.dispose();
        whiteTexture.dispose();
        FontHandler.dispose();

        if (shooterGui != null) {
            shooterGui.dispose();
        }
        System.exit(0);
    }


    @Override
    public void resize(int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        hudViewport.update(width, height, true);
        viewport.update(width, height, false);
        updateScreens(width, height);
    }

    public void updateScreens(int width, int height) {
        pathGui.updateScreen(width, height);
        if (shooterGui != null) {
            shooterGui.updateScreen(width, height);
        }
        configGUI.updateScreen(width, height);
        settingsGui.updateScreen(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        FileHandler.saveAuto(false);
    }

    public static @NotNull
    Config getConfig() {
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
        undoHandler.restoreState(new UndoState(autonomous, config), pathGui, cameraHandler);
        if (clearUndoHistory) {
            undoHandler.clearUndoHistory();
        }
        undoHandler.forceCreateUndoState();
    }
}
