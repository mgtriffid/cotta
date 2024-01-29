package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.ScreenAdapter
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.screens.game.graphics.GraphicsV2
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.input.CharacterInputComponent
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging

const val SCALE = 3

private val logger = KotlinLogging.logger {}

// TODO handle resize and pause and all the things
class GameScreen(
    private val gdxGame: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaClient: CottaClient

    private var nextTickAt: Long = -1
    private var tickLength: Long = -1
    private val graphics: GraphicsV2 = GraphicsV2()

    private lateinit var input: PannaClientGdxInput

    override fun show() {
        graphics.initialize()
        input = PannaClientGdxInput(graphics.viewport)
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

        draw(1.0f - (nextTickAt - now).toFloat() / tickLength.toFloat(), delta)
    }

    override fun dispose() {
        graphics.dispose()
    }

    private fun draw(alpha: Float, delta: Float) {
        if (!cottaClient.localPlayer.isReady()) {
            return
        }
        val state = getDrawableState(alpha)
        if (noDude(state)) {
            input.mayJoin = true
        }
        graphics.draw(state, cottaClient.localPlayer.playerId, delta, input.mayJoin)
    }

    private fun noDude(state: DrawableState) : Boolean {
        return state.entities.none {
            it.hasInputComponent(CharacterInputComponent::class) &&
                it.ownedBy == Entity.OwnedBy.Player(cottaClient.localPlayer.playerId)
        }
    }

    private fun getDrawableState(alpha: Float): DrawableState {
        return cottaClient.getDrawableState(alpha, DrawableComponent::class, PositionComponent::class)
    }
}
