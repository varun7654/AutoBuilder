package me.varun.autobuilder.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.varun.autobuilder.AutoBuilder;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Auto Builder";
		config.width = 1280;
		config.height = 720;
		new LwjglApplication(new AutoBuilder(), config);
	}
}
