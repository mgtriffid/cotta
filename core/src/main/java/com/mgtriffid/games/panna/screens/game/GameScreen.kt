package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaConfigStatic.height
import com.mgtriffid.games.panna.PannaConfigStatic.width
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.PannaGraphicsConfig
import com.mgtriffid.games.panna.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.roundToInt

const val SCALE = 3

private val logger = KotlinLogging.logger {}

// TODO handle resize and pause and all the things
class GameScreen(
    private val gdxGame: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaClient: CottaClient





    private var nextTickAt: Long = -1
    private var tickLength: Long = -1
    private val graphics: Graphics = Graphics()

    private lateinit var input: PannaClientGdxInput

    override fun show() {
        graphics.initialize()
        input = PannaClientGdxInput()
        val game = PannaGame()
        logger.debug { "Tick length is ${game.config.tickLength}" }
        tickLength = game.config.tickLength
        cottaClient = CottaClientFactory().create(game, input)
        cottaClient.initialize()
        nextTickAt = now()
    }

    /**
     * This is called rapidly by LibGDX game loop. Think of this as of the main loop body.
     */
    override fun render(delta: Float) {
        logger.trace { "${GameScreen::class.simpleName}#render called" }

        input.accumulate()

        var tickHappened = false
        val now = now()
        if (nextTickAt <= now) {
            cottaClient.tick()
            nextTickAt += tickLength
            tickHappened = true
        }

        if (tickHappened) {
            input.clear()
        }

        draw(1.0f - (nextTickAt - now).toFloat() / tickLength.toFloat())
    }

    override fun dispose() {
        graphics.dispose()

    }

    private fun draw(alpha: Float) {
        if (!cottaClient.localPlayer.isReady()) {
            return
        }
        val entities = getDrawableEntities(alpha)
        graphics.draw(entities, alpha, cottaClient.localPlayer.playerId)

    }

    private fun getDrawableEntities(alpha: Float): List<Entity> {
        return cottaClient.getDrawableEntities(alpha, DrawableComponent::class, PositionComponent::class)
    }
}
