package me.varun.autobuilder.config.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import me.varun.autobuilder.gui.elements.AbstractGuiButton;


public class UploadFileButton extends AbstractGuiButton {

    public UploadFileButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("upload-icon.png"), true));
    }

    @Override
    public boolean checkClick() {
        if (super.checkClick()) {
            Platform.runLater(() -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Open Resource File");
                        fileChooser.showOpenDialog(null);
                    }
            );
            return true;
        }
        return false;
    }
}
