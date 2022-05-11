package me.varun.autobuilder.config.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.gui.elements.AbstractGuiButton;

import java.io.File;
import java.util.concurrent.CompletableFuture;


public class UploadFileButton extends AbstractGuiButton {
    private final ConfigGUI configGUI;

    public UploadFileButton(int x, int y, int width, int height, ConfigGUI configGUI) {
        super(x, y, width, height, new Texture(Gdx.files.internal("upload-icon.png"), true));
        this.configGUI = configGUI;
    }

    @Override
    public boolean checkClick() {
        if (super.checkClick()) {
            CompletableFuture<File> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Open an Auto or Config File");
                        fileChooser.getExtensionFilters().addAll(
                                new FileChooser.ExtensionFilter("json files", "*.json", "config.json"));
                fileChooser.setInitialDirectory(AutoBuilder.getConfig().getAutoPath().getParentFile());
                        File file = fileChooser.showOpenDialog(null);
                        if (file != null) {
                            future.complete(file);
                        }
                    }
            );
            configGUI.setOpenedFile(future);
            return true;
        }
        return false;
    }
}
