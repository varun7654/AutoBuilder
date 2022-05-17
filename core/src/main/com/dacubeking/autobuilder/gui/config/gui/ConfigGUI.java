package com.dacubeking.autobuilder.gui.config.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class ConfigGUI {
    Color transparentWhite = new Color(1, 1, 1, 0.8f);

    UploadFileButton uploadFileButton = new UploadFileButton(60, 10, 40, 40, this);

    TextBlock dropToImport = new TextBlock(Fonts.ROBOTO, 30,
            new TextComponent("Drop here to switch to the auto").setColor(transparentWhite));

    Polygon boarderPolygon = new Polygon(new float[]{
            10, 10,
            Gdx.graphics.getWidth() - 10, 10,
            Gdx.graphics.getWidth() - 10, Gdx.graphics.getHeight() - 10,
            10, Gdx.graphics.getHeight() - 10
    });

    long lastConfigModified = 0;
    long lastAutoModified = 0;

    public boolean update() {
        uploadFileButton.checkHover();
        uploadFileButton.checkClick();


        try {
            long lastAutoModified = Files.getLastModifiedTime(AutoBuilder.getConfig().getAutoPath().toPath()).toMillis();
            long lastConfigModified =
                    Files.getLastModifiedTime(new File(AutoBuilder.USER_DIRECTORY + "/config.json").toPath()).toMillis();
//This is too problematic
//            if (lastConfigModified > this.lastConfigModified) {
//                this.lastConfigModified = lastConfigModified;
//                FileHandler.reloadConfig();
//            }
//
//            if (lastAutoModified > this.lastAutoModified) {
//                this.lastAutoModified = lastAutoModified;
//                FileHandler.reloadAuto();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable CompletableFuture<File> completableOpenFile;

    public void setOpenedFile(CompletableFuture<File> completedFile) {
        this.completableOpenFile = completedFile;
    }

    public void draw(ShapeDrawer shapeDrawer, Batch batch, Camera cam) {

        if (completableOpenFile != null) {
            if (completableOpenFile.isDone()) {
                @Nullable File file = completableOpenFile.join();
                if (file != null) {
                    FileHandler.handleFile(file);
                }
                completableOpenFile = null;
            }
        }

        if (completableNewAutoFile != null) {
            if (completableNewAutoFile.isDone()) {
                @Nullable File file = completableNewAutoFile.join();
                if (file != null) {
                    FileHandler.createNewAuto(file);
                }
                completableNewAutoFile = null;
            }
        }

        uploadFileButton.render(shapeDrawer, batch);

//        shapeDrawer.setColor(transparentWhite);
//        FontRenderer.renderText(batch, shapeDrawer,
//                (Gdx.graphics.getWidth() - dropToImport.getWidth() - 410) / 2,
//                (Gdx.graphics.getHeight() - dropToImport.getHeight()) / 2,
//                dropToImport);
//
//        shapeDrawer.polygon(boarderPolygon, 5, JoinType.POINTY);
    }

    public void updateScreen(int width, int height) {
        uploadFileButton.setPosition(width - uploadFileButton.getWidth() - 410 - 160, 10);
    }

    @Nullable CompletableFuture<File> completableNewAutoFile;

    public void setNewAutoFile(CompletableFuture<File> future) {
        this.completableNewAutoFile = future;
    }
}
