package com.mgtriffid.games.panna.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.screens.menu.MenuScreen

class DisconnectedScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var stage: Stage

    override fun show() {
        stage = Stage()
        stage.addActor(Label("Disconnected", MenuScreen.Styles.formInputLabelStyle))
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        if (Gdx.input.justTouched()) {
            game.setScreen(MenuScreen(game))
        }
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
