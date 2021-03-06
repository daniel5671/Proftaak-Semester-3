package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.DistressAndConflict;

import java.rmi.RemoteException;

public class DesktopLauncher {
	public static void main (String[] arg) throws RemoteException {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Distress and Conflict";
		config.useGL30 = true;
		config.resizable = true;
		config.fullscreen = false;
		config.height = 1080;
		config.width = 1920;
		config.foregroundFPS = 60;
		new LwjglApplication(new DistressAndConflict(), config);

	}
}
