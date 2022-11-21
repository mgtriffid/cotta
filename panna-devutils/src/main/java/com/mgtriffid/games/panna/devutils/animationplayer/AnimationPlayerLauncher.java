package com.mgtriffid.games.panna.devutils.animationplayer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mgtriffid.games.panna.devutils.animationplayer.gdxadapter.AnimationPlayerGdxGame;

public class AnimationPlayerLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Animation Player");
        config.setWindowedMode(400, 400);
        new Lwjgl3Application(new AnimationPlayerGdxGame(), config);
    }
}
