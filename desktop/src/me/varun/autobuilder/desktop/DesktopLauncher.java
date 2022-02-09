package me.varun.autobuilder.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import me.varun.autobuilder.AutoBuilder;

public class DesktopLauncher {


	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Auto Builder");
		config.setWindowedMode(1280, 720);
		config.setForegroundFPS(60);
		config.setIdleFPS(30);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
		new Lwjgl3Application(new AutoBuilder(), config);
	}
}
