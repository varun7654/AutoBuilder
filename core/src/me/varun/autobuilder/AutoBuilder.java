package me.varun.autobuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class AutoBuilder extends ApplicationAdapter {
    private SpriteBatch batch;
    public BitmapFont font;
    private Texture field;
    OrthographicCamera cam;
    Viewport viewport;

    CameraHandler cameraHandler;


    @Override
    public void create () {
        batch = new SpriteBatch();
        font = new BitmapFont();
        field = new Texture(Gdx.files.internal("field20.png"));

        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.x = Gdx.graphics.getWidth()/2f;
        cam.position.y = Gdx.graphics.getHeight()/2f;
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), cam);

        cameraHandler = new CameraHandler(cam);

        Gdx.app.getInput().setInputProcessor(cameraHandler);

    }

    @Override
    public void render () {
        cameraHandler.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(cam.combined); //Important

        batch.begin();
        batch.draw(field, 0, 0);
        batch.end();

    }

    @Override
    public void dispose () {
        batch.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height);
    }
}
