package com.mgtriffid.games.panna.screens.menu

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
import com.badlogic.gdx.scenes.scene2d.ui.Stack
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
const val UI_DEBUG = false

// One day I will learn how to do MVC / MVVM / MVP / BBC / FTM / OMG / QGD but now let it be a mess
class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {

    object UiConfig {
        const val statusPanelHeight = 140
        const val statusPanelWidth = 400
    }

    lateinit var textures: MenuTextures
    // here we have a scene with buttons and also some way to initiate connection
    lateinit var stage: Stage
    lateinit var loginButton: Button
    lateinit var statusPanelWindow: Window
    lateinit var characterListWindow: Window
    private val menuState = MenuState()
    private var authToken: AuthToken = AuthToken.NotAuthorized

    private val gson = Gson()

    object Styles {
        val formInputLabelStyle = LabelStyle(BitmapFont(), Color.WHITE)
    }

    override fun show() {
        textures = MenuTextures()
        prepareStage()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.batch.begin()
        stage.batch.draw(textures.background, 0f, 0f)
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
        table.debug = UI_DEBUG
        stage.addActor(table)
        val loginLabel = Label("login", Styles.formInputLabelStyle)
        table.add(loginLabel)
        // TODO use pref size
        val textFieldStyle = TextFieldStyle(
            BitmapFont(),
            Color.YELLOW,
            TextureRegionDrawable(textures.textInputCursor),
            null,
            NinePatchDrawable(
                NinePatch(
                    textures.textInput9Patch,
                    8, 8, 8, 8
                )
            )
        )
        val loginInput = TextField(
            "", textFieldStyle
        )
        table.add(loginInput).width(300f).height(50f).pad(10f)
        table.row()
        val passwordLabel = Label("password", Styles.formInputLabelStyle)
        table.add(passwordLabel)
        val passwordInput = TextField(
            "", textFieldStyle
        )
        passwordInput.isPasswordMode = true
        passwordInput.setPasswordCharacter('*')
        table.add(passwordInput).width(300f).height(50f).pad(10f)
        table.row()
        val buttonStyle = getLoginButtonStyle()
        loginButton = TextButton("login", buttonStyle)
        loginButton.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                logger.debug { "Login button clicked" }
                super.touchUp(event, x, y, pointer, button)
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
                logger.debug { "touchUp processing of loginButton complete" }
            }
        })

        table.add(loginButton).colspan(2)
    }

    private fun buildCharactersList() {
        characterListWindow = Window(
            "Characters list", Window.WindowStyle(
                BitmapFont(),
                Color.WHITE,
                null
            )
        )
        characterListWindow.debug = true
        characterListWindow.isMovable = false
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
        val setVisible = { visible: Boolean -> characterListWindow.isVisible = visible }
        characterListWindow.addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    State.IDLE -> setVisible(false)
                    State.AUTHORIZATION -> setVisible(false)
                    State.RETRIEVING_CHARACTER_LIST -> setVisible(false)
                    State.AUTHORIZATION_FAILED -> setVisible(false)
                    State.RETRIEVED_CHARACTER_LIST -> setVisible(true)
                    State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> setVisible(false)
                }
                return false
            }
        })
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
        logger.info { "Token is '${result.token}'" }
        authToken = AuthToken.Authorized(result.token)
    }

    private fun buildStatusPanel() {
        statusPanelWindow = Window(
            "", Window.WindowStyle(
                BitmapFont(),
                Color.WHITE,
                TextureRegionDrawable(textures.statusPanelBackground)
            )
        )
        statusPanelWindow.titleTable.isVisible = false
        statusPanelWindow.debug = UI_DEBUG
        statusPanelWindow.setPosition(
            game.config.width.toFloat() / 2 - UiConfig.statusPanelWidth / 2,
            game.config.height.toFloat() / 2 - UiConfig.statusPanelHeight / 2
        )
        statusPanelWindow.setSize(UiConfig.statusPanelWidth.toFloat(), UiConfig.statusPanelHeight.toFloat())
        addStatusText()
        val stack = Stack()
        addDialogOkayTextButton(stack)
        addDialogCancelTextButton(stack)
        statusPanelWindow.row()
        statusPanelWindow.add(stack)
        stage.addActor(statusPanelWindow)

    }

    private fun addStatusText() {
        val statusPanelTextLabel = Label("neuzhto", Styles.formInputLabelStyle)
        statusPanelWindow.add(statusPanelTextLabel).expandX()

        val setVisible: (Boolean) -> Unit = { value -> statusPanelWindow.isVisible = value }
        val setText: (String) -> Unit = { text -> statusPanelTextLabel.setText(text) }
        val value = object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    State.IDLE -> {
                        setVisible(false)
                    }

                    State.AUTHORIZATION -> {
                        setVisible(true)
                        setText("Authorization...")
                    }

                    State.RETRIEVING_CHARACTER_LIST -> {
                        setVisible(true)
                        setText("Retrieving character list...")
                    }

                    State.RETRIEVED_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        setVisible(true)
                        setText("Failed to retrieve characters list")
                    }

                    State.AUTHORIZATION_FAILED -> {
                        setVisible(true)
                        setText("Authorization failed")
                    }
                }
                return false
            }
        }
        statusPanelTextLabel.addAction(value)
    }

    private fun addDialogOkayTextButton(stack: Stack) {
        val buttonStyle = getDialogButtonStyle()

        val textButton = TextButton("Okay", buttonStyle)
        stack.add(textButton)
        val setVisible: (Boolean) -> Unit = { value -> textButton.isVisible = value }
        val setText: (String) -> Unit = { text -> textButton.setText(text) }
        textButton.addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    State.IDLE -> {
                        setVisible(false)
                    }

                    State.AUTHORIZATION -> {
                        setVisible(false)
                    }

                    State.RETRIEVING_CHARACTER_LIST -> {
                        setVisible(false)
                        setText("Retrieving character list...")
                    }

                    State.RETRIEVED_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        setVisible(true)
                    }

                    State.AUTHORIZATION_FAILED -> {
                        setVisible(true)
                    }
                }
                return false
            }
        })
        textButton.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                menuState.idle()
            }
        })
    }

    private fun getLoginButtonStyle(): TextButton.TextButtonStyle {
        return getButtonStyle(
            upTexture = textures.loginButtonUpTexture,
            downTexture = textures.loginButtonDownTexture
        )
    }

    private fun getDialogButtonStyle(): TextButton.TextButtonStyle {
        return getButtonStyle(
            upTexture = textures.dialogButtonUpTexture,
            downTexture = textures.dialogButtonDownTexture
        )
    }

    private fun getButtonStyle(upTexture: Texture, downTexture: Texture): TextButton.TextButtonStyle {
        val upRegion = TextureRegion(upTexture)
        val downRegion = TextureRegion(downTexture)
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.up = TextureRegionDrawable(upRegion)
        buttonStyle.down = TextureRegionDrawable(downRegion)
        buttonStyle.font = Styles.formInputLabelStyle.font
        return buttonStyle
    }

    private fun addDialogCancelTextButton(stack: Stack) {
        val buttonStyle = getDialogButtonStyle()

        val textButton = TextButton("Cancel", buttonStyle)
        stack.add(textButton)
        val setVisible: (Boolean) -> Unit = { value -> textButton.isVisible = value }
        textButton.addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    State.IDLE -> {
                        setVisible(false)
                    }

                    State.AUTHORIZATION -> {
                        setVisible(true)
                    }

                    State.RETRIEVING_CHARACTER_LIST -> {
                        setVisible(true)
                    }

                    State.RETRIEVED_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        setVisible(false)
                    }

                    State.AUTHORIZATION_FAILED -> {
                        setVisible(false)
                    }
                }
                return false
            }
        })
    }

    override fun dispose() {
        textures.dispose()
    }

    enum class State {
        IDLE,
        AUTHORIZATION,
        RETRIEVING_CHARACTER_LIST,
        AUTHORIZATION_FAILED,
        RETRIEVED_CHARACTER_LIST,
        FAILED_TO_RETRIEVE_CHARACTERS_LIST,
    }

    sealed class AuthToken {
        object NotAuthorized : AuthToken()
        data class Authorized(val token: String) : AuthToken()
    }
}
