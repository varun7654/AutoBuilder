package me.varun.autobuilder.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import me.varun.autobuilder.AutoBuilder;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Auto Builder");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(1000);
        config.setIdleFPS(1000);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        config.setWindowIcon(FileType.Internal, "icon.png");
        final AutoBuilder app = AutoBuilder.getInstance();
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void filesDropped(String[] files) {
                if (files != null && files.length == 1) {
                    if (files[0].endsWith(".json")) {
                        app.loadFile(files[0]);
                    }
                }
            }
        });

        new Lwjgl3Application(app, config);
    }
}
