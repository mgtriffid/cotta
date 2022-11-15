package com.mgtriffid.games.panna;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		PannaConfig pannaConfig = PannaConfigStatic.INSTANCE;
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Cotta");
		config.setWindowedMode(pannaConfig.getWidth(), pannaConfig.getHeight());
		new Lwjgl3Application(new PannaGdxGame(pannaConfig), config);
	}
}
