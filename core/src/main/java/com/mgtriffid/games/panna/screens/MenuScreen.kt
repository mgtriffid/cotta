package com.mgtriffid.games.panna.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.mgtriffid.games.panna.PannaGdxGame

class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    // here we have a scene with buttons and also some way to initiate connection
    lateinit var stage: Stage
    lateinit var button: Button
    lateinit var backgroundTexture: Texture
    lateinit var statusPanel: Table

    object Styles {
        val formInputLabelStyle = LabelStyle(BitmapFont(), Color.WHITE)
        val textFieldStyle = TextFieldStyle(
            BitmapFont(),
            Color.YELLOW,
            null,
            null,
            NinePatchDrawable(
                NinePatch(
                    Texture("textfield_9patch_atlas.png"),
                    8, 8, 8, 8
                )
            )
        )
    }

    override fun show() {
        prepareStage()
        backgroundTexture = Texture("menu_bg.png")
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.batch.begin()
        stage.batch.draw(backgroundTexture, 0f, 0f)
        stage.batch.end()
        stage.draw()
    }

    private fun prepareStage() {
        stage = Stage()

        Gdx.input.inputProcessor = stage

        buildLoginForm()
        buildStatusPanel()
    }

    private fun buildLoginForm() {
        val table = Table()
        table.setFillParent(true)
        table.debug = true
        stage.addActor(table)
        val loginInput = TextField("", Styles.textFieldStyle)
        val loginLabel = Label("login", Styles.formInputLabelStyle)
        table.add(loginLabel)
        table.add(loginInput).width(300f).height(50f).pad(10f)
        table.row()
        val passwordLabel = Label("password", Styles.formInputLabelStyle)
        table.add(passwordLabel)
        val passwordInput = TextField("", Styles.textFieldStyle)
        passwordInput.isPasswordMode = true
        passwordInput.setPasswordCharacter('*')
        table.add(passwordInput).width(300f).height(50f).pad(10f)
        table.row()
        val upTexture = Texture("blue_button_up.png")
        val downTexture = Texture("blue_button_down.png")

        val upRegion = TextureRegion(upTexture)
        val downRegion = TextureRegion(downTexture)
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.up = TextureRegionDrawable(upRegion)
        buttonStyle.down = TextureRegionDrawable(downRegion)
        buttonStyle.font = Styles.formInputLabelStyle.font
        button = TextButton("login", buttonStyle)
        button.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                Gdx.app.log("MenuScreen", "Button clicked")
                "http://127.0.0.1:4567/login".httpPost()
                    .body("{\n  \"username\": \"${loginInput.text}\",\n  \"password\": \"${passwordInput.text}\"\n}\n")
                    .responseString { req, resp, result ->
                        when (result) {
                            is Result.Success -> println(result.get())
                            is Result.Failure -> println(result.getException())
                        }
                    }
                return true
            }
        })

        table.add(button).colspan(2)
    }

    private fun buildStatusPanel() {
        statusPanel = Table()
        statusPanel.setFillParent(true)
        val statusPanelTextLabel = Label("neuzhto", Styles.formInputLabelStyle)
        statusPanel.add(statusPanelTextLabel)
        stage.addActor(statusPanel)
        statusPanelTextLabel.addAction(object : Action() {
            var acc = 0f
            override fun act(delta: Float): Boolean {
                acc += delta
                println("acc = $acc")
                statusPanelTextLabel.isVisible = (acc.toInt() % 2) == 1
                println("statusPanelTextLabel.isVisible = ${statusPanelTextLabel.isVisible}")
                return false
            }
        })
    }
}
