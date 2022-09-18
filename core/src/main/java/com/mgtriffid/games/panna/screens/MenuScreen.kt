package com.mgtriffid.games.panna.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.mgtriffid.games.panna.PannaGdxGame
import org.w3c.dom.Text

class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    // here we have a scene with buttons and also some way to initiate connection
    lateinit var stage: Stage
    lateinit var button: Button
    lateinit var backgroundTexture: Texture

    override fun show() {
        prepareStage()
        backgroundTexture = Texture("menu_bg.png")
    }

    private fun prepareStage() {
        prepareTextFieldNinePatch()
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
            prepareTextFieldNinePatch()
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
                "http://127.0.0.1:4567/login".httpPost().body("{\n  \"username\": \"${loginInput.text}\",\n  \"password\": \"${passwordInput.text}\"\n}\n")
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

        table.add(button)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.batch.begin()
        stage.batch.draw(backgroundTexture, 0f, 0f)
        stage.batch.end()
        stage.draw()
    }

    private fun prepareTextFieldNinePatch(): NinePatchDrawable {
        val ninePatch: NinePatch = NinePatch(Texture("textfield_9patch_atlas.png"), 8, 8, 8, 8)
        return NinePatchDrawable(ninePatch)
    }
}
