package me.matistan;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import static me.matistan.Main.SCREEN_HEIGHT;
import static me.matistan.Main.SCREEN_WIDTH;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Game of Life");
		config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
		new Lwjgl3Application(new Main(), config);
	}
}