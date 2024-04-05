package com.mgtriffid.games.panna.screens.game.debug

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.panna.screens.menu.MenuScreen

class MetricsDisplay(spriteBatch: SpriteBatch) {
    private val viewport: Viewport = FitViewport(960f, 540f)
    val stage: Stage = Stage(viewport, spriteBatch)
    private val bufferLengthDisplay: Label

    init {
        val table = Table()
        val bufferLength = Label("Buffer length:", MenuScreen.Styles.formInputLabelStyle)
        bufferLengthDisplay = Label("0", MenuScreen.Styles.formInputLabelStyle)
        table.add(bufferLength)
        table.add(bufferLengthDisplay)
        table.setPosition(800f, 500f)
        stage.addActor(table)
        stage.isDebugAll = true
    }

    fun updateBufferLength(bufferLength: Double) {
        bufferLengthDisplay.setText(bufferLength.toString())
    }

    fun dispose() {
        stage.dispose()
    }
}
