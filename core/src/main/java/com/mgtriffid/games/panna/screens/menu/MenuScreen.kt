package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.graphics.actors.AnimatedImage
import com.mgtriffid.games.panna.graphics.animation.Animation
import com.mgtriffid.games.panna.graphics.animation.FrameConfig
import com.mgtriffid.games.panna.screens.menu.components.LoginForm
import com.mgtriffid.games.panna.screens.menu.components.StatusDialogWindow
import com.mgtriffid.games.panna.screens.menu.components.characterlist.CharacterListModel
import com.mgtriffid.games.panna.screens.menu.components.characterlist.CharacterListWindow
import com.mgtriffid.games.panna.screens.menu.components.characterlist.CharacterModel
import com.mgtriffid.games.panna.shared.lobby.CharactersResponse
import com.mgtriffid.games.panna.shared.lobby.SuccessfulLoginResponse
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
const val UI_DEBUG = false

class MenuScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {

    object UiConfig {
        const val STATUS_PANEL_WIDTH = 400f
        const val STATUS_PANEL_HEIGHT = 140f
        const val CHARACTERS_TABLE_WIDTH = 800f
        const val CHARACTERS_TABLE_HEIGHT = 450f
        const val CHARACTER_CELL_WIDTH = 180f
        const val CHARACTER_CELL_HEIGHT = 180f
    }

    lateinit var textures: MenuTextures

    // here we have a scene with buttons and also some way to initiate connection
    lateinit var stage: Stage
    private val characterListModel = CharacterListModel()
    lateinit var menuState: MenuState
    private var authToken: AuthToken = AuthToken.NotAuthorized

    private val gson = Gson()

    object Styles {
        val formInputLabelStyle = LabelStyle(BitmapFont(), Color.WHITE)
    }

    override fun show() {
        textures = MenuTextures()
        menuState = MenuState()
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
        stage.addActor(AnimatedImage(createAnimation()))
        Gdx.input.inputProcessor = stage

        buildLoginForm()
        buildStatusPanel()
        buildCharactersList()
    }

    private fun buildLoginForm() {
        val loginForm = LoginForm(textures)
        loginForm.onClick = {
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
        stage.addActor(loginForm.table)
    }

    private fun buildCharactersList() {
        val characterListWindow = CharacterListWindow(characterListModel, textures)
        characterListWindow.window.isVisible = false
        characterListWindow.onClose = menuState::idle
        characterListWindow.onEnter = ::setGameScreen
        val setVisible = { visible: Boolean -> characterListWindow.window.isVisible = visible }
        characterListWindow.window.addAction(object : Action() {
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
        stage.addActor(characterListWindow.window)
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
                                    val charactersResponse = gson.fromJson(result.value, CharactersResponse::class.java)
                                    logger.debug { "Got ${charactersResponse.characters} characters in response" }
                                    updateCharacterListModel(charactersResponse)
                                    menuState.characterListRetrieved()
                                }

                                is Result.Failure -> {
                                    logger.warn(result.getException()) { "Could not retrieve character list" }
                                    menuState.failedToRetrieveCharactersList()
                                }
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

    private fun updateCharacterListModel(result: CharactersResponse) {
        characterListModel.updateCharacters(result.characters.map { CharacterModel(
            name = it.name,
            color = CharacterModel.CharacterColor(it.color.r, it.color.g, it.color.b)
        ) })
    }

    private fun buildStatusPanel() {
        stage.addActor(
            StatusDialogWindow(
                menuState = menuState,
                textures = textures,
                config = game.config
            ).statusPanelWindow
        )
    }

    private fun setGameScreen() {
        game.screen = GameScreen(game)
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

    private fun createAnimation(): Animation {
        val textureSheet = Texture(
            "characters-free-sprites/Woodcutter/Woodcutter_idle.png"
        )
        val regions = TextureRegion.split(
            textureSheet, 48, 48
        )
        val frameRegions = regions.flatMap { it.toList() }
        return Animation(frameRegions.map { FrameConfig(250, it) })
    }
}
