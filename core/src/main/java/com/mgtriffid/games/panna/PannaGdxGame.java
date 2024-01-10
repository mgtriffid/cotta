package com.mgtriffid.games.panna;

import com.badlogic.gdx.Game;
import com.mgtriffid.games.panna.screens.menu.MenuScreen;

import static com.mgtriffid.games.cotta.core.logging.LoggingConfigurationKt.configureLogging;

public class PannaGdxGame extends Game {

	public final PannaConfig config;

	public PannaGdxGame(PannaConfig pannaConfig) {
		this.config = pannaConfig;
	}

	@Override
	public void create () {
		configureLogging();
		setScreen(new MenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
}
