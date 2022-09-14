package com.mgtriffid.games.panna.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mgtriffid.games.panna.PannaGdxGame

class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    lateinit var stage: Stage
    lateinit var button: Button

    override fun show() {
        stage = Stage()
        Gdx.input.inputProcessor = stage

        val table = Table()
        table.setFillParent(true)
        table.debug = true
        stage.addActor(table)

        val upTexture = Texture("blue_button_up.png")
        val downTexture = Texture("blue_button_down.png")

        val upRegion = TextureRegion(upTexture)
        val downRegion = TextureRegion(downTexture)
        val buttonStyle = ButtonStyle()
        buttonStyle.up = TextureRegionDrawable(upRegion)
        buttonStyle.down = TextureRegionDrawable(downRegion)
        button = Button()
        button.style = buttonStyle

        table.add(button)

    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }
}
