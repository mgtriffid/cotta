package com.mgtriffid.games.panna.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseResultHandler
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.mgtriffid.games.panna.PannaGdxGame

class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    // here we have a scene with buttons and also some way to initiate connection
    lateinit var stage: Stage
    lateinit var button: Button

    override fun show() {
        prepareStage()
    }

    private fun prepareStage() {
        stage = Stage()
        Gdx.input.inputProcessor = stage

        val table = Table()
        table.setFillParent(true)
        table.debug = true
        stage.addActor(table)
        val textFieldStyle = TextFieldStyle(
            BitmapFont(),
            Color.YELLOW,
            null,
            null,
            null
        )
        val loginInput = TextField("", textFieldStyle)
        table.add(loginInput)
        table.row()
        val passwordInput = TextField("", textFieldStyle)
        table.add(passwordInput)
        table.row()
        val upTexture = Texture("blue_button_up.png")
        val downTexture = Texture("blue_button_down.png")

        val upRegion = TextureRegion(upTexture)
        val downRegion = TextureRegion(downTexture)
        val buttonStyle = ButtonStyle()
        buttonStyle.up = TextureRegionDrawable(upRegion)
        buttonStyle.down = TextureRegionDrawable(downRegion)
        button = Button()
        button.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                Gdx.app.log("MenuScreen", "Button clicked")
                "http://127.0.0.1:4567/login".httpPost().body("{\n  \"username\": \"odnako\",\n  \"password\": \"odnako123\"\n}\n")
                .responseString { req, resp, result ->
                    when (result) {
                        is Result.Success -> println(result.get())
                        is Result.Failure -> println(result.getException())
                    }
                }
                return true
            }
        })
        button.style = buttonStyle

        // how to call HTTP and add a callback

        table.add(button)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }
}
