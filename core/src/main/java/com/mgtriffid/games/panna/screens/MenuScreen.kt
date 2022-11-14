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
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.shared.lobby.SuccessfulLoginResponse
import mu.KotlinLogging
import java.lang.IllegalStateException

private val logger = KotlinLogging.logger {}
// One day I will learn how to do MVC / MVVM / MVP / BBC / FTM / OMG / QGD but now let it be a mess
class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    // here we have a scene with buttons and also some way to initiate connection
    lateinit var stage: Stage
    lateinit var button: Button
    lateinit var backgroundTexture: Texture
    lateinit var cursorTexture: Texture
    lateinit var statusPanel: Table
    lateinit var characterListWindow: Window
    private val menuState = MenuState()
    private var authToken: AuthToken = AuthToken.NotAuthorized

    private val gson = Gson()

    object Styles {
        val formInputLabelStyle = LabelStyle(BitmapFont(), Color.WHITE)
        // todo ensure these are not static
        val textFieldStyle = TextFieldStyle(
            BitmapFont(),
            Color.YELLOW,
            TextureRegionDrawable(Texture("cursor.png")),
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
        buildCharactersList()
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
        button.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                menuState.startAuthorization()
                "http://127.0.0.1:4567/login".httpPost()
                    .body("{\n  \"username\": \"${loginInput.text}\",\n  \"password\": \"${passwordInput.text}\"\n}\n")
                    .responseString { req, resp, result ->
                        when (result) {
                            is Result.Success -> {
                                rememberToken(gson.fromJson(result.value, SuccessfulLoginResponse::class.java))
                                retrieveCharacterList()
                            }

                            is Result.Failure -> menuState.authorizationFailed()
                        }
                    }
            }
        })

        table.add(button).colspan(2)
    }

    private fun buildCharactersList() {
        characterListWindow = Window("Characters list", Window.WindowStyle(
            BitmapFont(),
            Color.WHITE,
            null
        ))
        characterListWindow.debug = true
        characterListWindow.isMovable = true
        characterListWindow.padTop(20f)
        characterListWindow.isResizable = false
        characterListWindow.setSize(800f, 450f)
        characterListWindow.isMovable = false
        characterListWindow.setPosition(240f, 135f)
        val charactersTable = Table()
        charactersTable.debug = true
        charactersTable.pad(5f)
        characterListWindow.add(charactersTable)
        characterListWindow.isVisible = false
        stage.addActor(characterListWindow)
    }

    private fun retrieveCharacterList() {
        authToken.let {
            when (it) {
                AuthToken.NotAuthorized -> throw IllegalStateException()
                is AuthToken.Authorized -> {
                    menuState.startRetrievingCharacterList()
                    "http://127.0.0.1:4567/characters".httpGet().header("token" to it.token)
                        .responseString { _, resp, result ->
                            when (result) {
                                is Result.Success -> {
                                    menuState.characterListRetrieved()
                                }

                                is Result.Failure -> menuState.failedToRetrieveCharactersList()
                            }
                        }

                }
            }
        }
    }

    private fun rememberToken(result: SuccessfulLoginResponse) {
        logger.info { "Token is ${result.token}" }
        authToken = AuthToken.Authorized(result.token)
    }

    private fun buildStatusPanel() {
        statusPanel = Table()
        statusPanel.debug = true
        statusPanel.setFillParent(true)
        val statusPanelTextLabel = Label("neuzhto", Styles.formInputLabelStyle)
        statusPanel.add(statusPanelTextLabel)
        stage.addActor(statusPanel)
        statusPanelTextLabel.addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    State.IDLE -> {
                        statusPanel.isVisible = false
                    }
                    State.AUTHORIZATION -> {
                        statusPanel.isVisible = true
                        statusPanelTextLabel.setText("Authorizing...")
                    }
                    State.RETRIEVING_CHARACTER_LIST -> {
                        statusPanel.isVisible = true
                        statusPanelTextLabel.setText("Retrieving character list...")
                    }
                    State.RETRIEVED_CHARACTER_LIST -> {
                        statusPanel.isVisible = false
                    }
                    State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        statusPanel.isVisible = true
                        statusPanelTextLabel.setText("Failed to retrieve characters list")
                    }
                    State.AUTHORIZATION_FAILED -> {
                        statusPanel.isVisible = true
                        statusPanelTextLabel.setText("Authorization failed")
                    }
                }
                return false
            }
        })
    }

    inner class MenuState {
        var state = State.IDLE

        fun startAuthorization() {
            state = State.AUTHORIZATION
        }

        fun startRetrievingCharacterList() {
            logger.info { "Retrieving character list" }
            state = State.RETRIEVING_CHARACTER_LIST
        }

        fun authorizationFailed() {
            logger.info { "Authorization failed" }
            state = State.AUTHORIZATION_FAILED
        }

        fun characterListRetrieved() {
            state = State.RETRIEVED_CHARACTER_LIST
        }

        fun failedToRetrieveCharactersList() {
            state = State.FAILED_TO_RETRIEVE_CHARACTERS_LIST
        }
    }

    enum class State {
        IDLE,
        AUTHORIZATION,
        RETRIEVING_CHARACTER_LIST,
        AUTHORIZATION_FAILED,
        RETRIEVED_CHARACTER_LIST,
        FAILED_TO_RETRIEVE_CHARACTERS_LIST,
    }
}

sealed class AuthToken {
    object NotAuthorized : AuthToken()
    data class Authorized(val token: String) : AuthToken()
}
