package me.varun.autobuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.path.PathGui;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.net.NetworkTablesHelper;
import me.varun.autobuilder.net.Serializer;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.pathing.PathRenderer.PointChange;
import me.varun.autobuilder.pathing.PointRenderer;
import me.varun.autobuilder.serialization.Autonomous;
import me.varun.autobuilder.serialization.GuiSerializer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.trajectory.constraint.CentripetalAccelerationConstraint;
import me.varun.autobuilder.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoBuilder extends ApplicationAdapter {
    public static final float POINT_SCALE_FACTOR = 129.7007874015748f; //153.719228856023f; 2020 field
    public static final float LINE_THICKNESS = 4;
    public static final float POINT_SIZE = 8;

    public static final float ROBOT_WIDTH = 36.3375f * 0.0254f;
    public static final float ROBOT_HEIGHT = 36.1875f * 0.0254f;
    public static BitmapFont font;
    public static ShaderProgram fontShader;
    public static ArrayList<TrajectoryConstraint> trajectoryConstraints = new ArrayList<>();
    public static double maxVelocityMetersPerSecond;
    public static double maxAccelerationMetersPerSecondSq;

    static {
        maxVelocityMetersPerSecond = 80 * .0254;
        maxAccelerationMetersPerSecondSq = 140 * 0.0254;
        trajectoryConstraints.add(new CentripetalAccelerationConstraint(80 * 0.0254));
    }

    private final Vector3 mousePos = new Vector3();
    private final Vector3 lastMousePos = new Vector3();
    @NotNull OrthographicCamera cam;
    @NotNull Viewport viewport;
    @NotNull CameraHandler cameraHandler;
    @NotNull Viewport hudViewport;
    @NotNull OrthographicCamera hudCam;
    @NotNull Preferences preferences;
    @NotNull PointRenderer origin;
    @NotNull ExecutorService pathingService = Executors.newFixedThreadPool(1);
    @NotNull PathGui pathGui;
    @NotNull InputEventThrower inputEventThrower = new InputEventThrower();
    @NotNull UndoHandler undoHandler = UndoHandler.getInstance();
    NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();
    private SpriteBatch batch;
    private SpriteBatch hudBatch;
    private Texture field;
    private ShapeDrawer shapeRenderer;
    private ShapeDrawer hudShapeRenderer;
    private static Config config;

    public static void handleCrash(Exception e) {
        e.printStackTrace();
        System.out.println("Oops Something went wrong during fame " + Gdx.graphics.getFrameId());
        Gdx.app.exit();
    }

    @Override
    public void create() {
        File configFile = new File(Gdx.files.getExternalStoragePath() + "/AppData/Roaming/AutoBuilder/config.json");
        configFile.getParentFile().mkdirs();
        try {
            config = (Config) Serializer.deserializeFromFile(configFile, Config.class);
        } catch (IOException e) {
            ArrayList<String> methods = new ArrayList<>();
            methods.add("print");
            methods.add("deployIntake");
            methods.add("undeployIntake");
            methods.add("intakeOn");
            methods.add("intakeOff");
            methods.add("intakeReverse");
            methods.add("snailOn");
            methods.add("snailOff");
            methods.add("snailReverse");
            methods.add("frontActive");
            methods.add("frontInactive");
            methods.add("frontReverse");
            methods.add("visionIdle");
            methods.add("visionWin");
            methods.add("visionAim");
            methods.add("setShooterSpeed");
            methods.add("fireShooter");
            methods.add("stopFiringShooter");
            methods.add("shootBalls");
            methods.add("turnOnIntakeTrack");
            methods.add("turnOffIntakeTrack");

            config = new Config(methods);
            e.printStackTrace();
        }


        networkTables.start();

        Gdx.app.getInput().setInputProcessor(inputEventThrower);

        hudBatch = new SpriteBatch();
        hudShapeRenderer = new ShapeDrawer(hudBatch, new TextureRegion(new Texture(Gdx.files.internal("white.png"))));

        batch = new SpriteBatch();
        shapeRenderer = new ShapeDrawer(batch, new TextureRegion(new Texture(Gdx.files.internal("white.png"))));

        field = new Texture(Gdx.files.internal("field21.png"), true);
        field.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);

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

        preferences = Gdx.app.getPreferences("me.varun.autobuilder.prefs");

        origin = new PointRenderer(preferences.getFloat("ORIGIN_POINT_X", 0), preferences.getFloat("ORIGIN_POINT_Y", 0),
                Color.ORANGE, POINT_SIZE);

        preferences.flush();

        //TODO: Looks like the texture is messed up and it makes it look really ugly
        Texture texture = new Texture(Gdx.files.internal("font/arial.png"), true);
        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);

        //texture.setAnisotropicFilter(8);


        font = new BitmapFont(Gdx.files.internal("font/arial.fnt"), new TextureRegion(texture), false);

        fontShader = new ShaderProgram(Gdx.files.internal("font/font.vert"), Gdx.files.internal("font/font.frag"));
        if (!fontShader.isCompiled()) {
            Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
        }

        pathGui = new PathGui(hudViewport, font, fontShader, inputEventThrower, pathingService, cameraHandler);

        File file = new File(Gdx.files.getExternalStoragePath() + "/AppData/Roaming/AutoBuilder/data.json");
        file.getParentFile().mkdirs();

        try {
            Autonomous autonomous = Serializer.deserializeAutoFromFile(file);
            undoHandler.restoreState(autonomous, pathGui, fontShader, font, inputEventThrower, cameraHandler);
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
        df.setMaximumFractionDigits(5);
    }

    private void draw() {
        //Clear everything
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT |
                (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        //Initialize our camera for the batch
        batch.setProjectionMatrix(cam.combined);
        shapeRenderer.setPixelSize(cam.zoom/8);

        //Draw the image
        batch.begin();
        batch.draw(field, -422f, -589f);

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
            shapeRenderer.line(pos1[0] * POINT_SCALE_FACTOR, pos1[1] * POINT_SCALE_FACTOR, pos2[0] * POINT_SCALE_FACTOR, pos2[1] * POINT_SCALE_FACTOR, Color.WHITE, LINE_THICKNESS);
        }

        batch.end();


        hudBatch.setProjectionMatrix(hudCam.combined);

        hudBatch.begin();
        hudBatch.setShader(fontShader);

        font.getData().setScale(0.2f);
        font.setColor(Color.WHITE);
        font.draw(hudBatch, "FPS: " + Gdx.graphics.getFramesPerSecond() + ", " + df.format(Gdx.graphics.getDeltaTime()* 1000) + " ms", 0, 12);

        hudBatch.setShader(null);


        pathGui.render(hudShapeRenderer, hudBatch, hudCam);

        hudBatch.end();


    }

    private void update() {
        undoHandler.update(pathGui, fontShader, font, inputEventThrower, cameraHandler);
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(mousePos);

        boolean somethingMoved = false;

        PathRenderer lastPathRender = null;
        PointChange lastPointChange = PointChange.NONE;
        boolean pointDeleted = false;
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            if (guiItem instanceof TrajectoryItem) {
                PathRenderer pathRenderer = ((TrajectoryItem) guiItem).getPathRenderer();
                //It's ok if lastPose2d is null if PointChange != LAST
                Pose2d lastPose2d = null;
                if (lastPointChange == PointChange.LAST) {
                    lastPose2d = lastPathRender.getPoint2DList().get(lastPathRender.getPoint2DList().size() - 1);
                }
                lastPointChange = pathRenderer.update(cam, mousePos, lastMousePos, lastPointChange, lastPose2d, somethingMoved);

                if (lastPointChange != PointChange.NONE) {
                    somethingMoved = true;
                }

                if (lastPointChange == PointChange.REMOVAL) {
                    pointDeleted = true;
                }

                lastPathRender = pathRenderer;
            }
        }

        //Don't add points if we've just deleted one
        if (!pointDeleted) {
            boolean pointAdded = false;
            for (AbstractGuiItem guiItem : pathGui.guiItems) {
                if (guiItem instanceof TrajectoryItem) {
                    PathRenderer pathRenderer = ((TrajectoryItem) guiItem).getPathRenderer();
                    if (!pointAdded && PointChange.ADDITION == pathRenderer.addPoints(mousePos)) {
                        pointAdded = true;
                    }
                }
            }
        }
        boolean onGui = pathGui.update();
        somethingMoved = somethingMoved || onGui;

        lastMousePos.set(mousePos);
        cameraHandler.update(somethingMoved, onGui);

        networkTables.updateRobotPath();
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        font.dispose();
        pathGui.dispose();
    }


    @Override
    public void resize(int width, int height) {
        hudViewport.update(width, height, true);
        viewport.update(width, height);

        pathGui.updateScreen(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        File file = new File(Gdx.files.getExternalStoragePath() + "/AppData/Roaming/AutoBuilder/data.json");
        File configFile = new File(Gdx.files.getExternalStoragePath() + "/AppData/Roaming/AutoBuilder/config.json");
        file.getParentFile().mkdirs();
        configFile.getParentFile().mkdirs();

        try {
            Serializer.serializeToFile(GuiSerializer.serializeAutonomous(pathGui.guiItems), file);
            configFile.createNewFile();
            Serializer.serializeToFile(config, configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Config getConfig(){
        return config;
    }
}
