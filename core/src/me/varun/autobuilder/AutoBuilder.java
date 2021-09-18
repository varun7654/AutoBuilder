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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.varun.autobuilder.pathgenerator.PathGenerator;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.pathing.PointRenderer;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryConfig;
import me.varun.autobuilder.wpi.math.trajectory.constraint.CentripetalAccelerationConstraint;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoBuilder extends ApplicationAdapter {
    public static final float POINT_SCALE_FACTOR = 50f;

    private SpriteBatch batch;
    private SpriteBatch batchHud;
    public BitmapFont font;
    private Texture field;
    private ShapeRenderer shapeRenderer;

    OrthographicCamera cam;
    Viewport viewport;

    CameraHandler cameraHandler;

    Preferences preferences;

    PointRenderer origin;

    ArrayList<PathRenderer> pathRenderers = new ArrayList<>();

    ExecutorService pathingService = Executors.newFixedThreadPool(1);

    public static TrajectoryConfig TRAJECTORY_CONSTRAINTS;
    static {
        TRAJECTORY_CONSTRAINTS = new TrajectoryConfig(5, 1.5);
        TRAJECTORY_CONSTRAINTS.addConstraint(new CentripetalAccelerationConstraint(1));
    }

    @Override
    public void create () {
        batch = new SpriteBatch();
        batchHud = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        field = new Texture(Gdx.files.internal("field20.png"));

        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.x = Gdx.graphics.getWidth()/2f;
        cam.position.y = Gdx.graphics.getHeight()/2f;
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), cam);

        cameraHandler = new CameraHandler(cam);

        Gdx.app.getInput().setInputProcessor(cameraHandler);

        preferences =  Gdx.app.getPreferences("me.varun.autobuilder.prefs");

        origin = new PointRenderer(preferences.getFloat("ORIGIN_POINT_X", 0), preferences.getFloat("ORIGIN_POINT_Y", 0),
                Color.ORANGE, 5);

        preferences.flush();

        pathRenderers.add(PathGenerator.genPaths(pathingService));

    }

    private final Vector3 mousePos = new Vector3();
    private final Vector3 lastMousePos = new Vector3();

    @Override
    public void render () {

        update();

        draw();
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT |
                (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));


        batch.setProjectionMatrix(cam.combined); //Important

        batch.begin();
        batch.draw(field, 0, 0);
        batch.end();

        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        origin.draw(shapeRenderer, cam);

        for (PathRenderer pathRenderer : pathRenderers) {
            pathRenderer.render(shapeRenderer, cam);
        }
        shapeRenderer.end();

        batchHud.begin();
        font.draw(batchHud, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, 12);
        batchHud.end();
    }

    private void update() {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(mousePos);

        boolean moving = false;

        for (PathRenderer pathRenderer : pathRenderers) {
            moving = moving | pathRenderer.update(cam, mousePos, lastMousePos);
        }

        lastMousePos.set(mousePos);
        cameraHandler.update(moving);
    }

    @Override
    public void dispose () {
        batch.dispose();
        batchHud.dispose();
        font.dispose();
    }




    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height);
    }
}
