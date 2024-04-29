package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.client.InterpolationAlphas
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.gdx.CottaGdxAdapter
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.screens.game.graphics.GraphicsV2
import com.mgtriffid.games.panna.shared.game.ConfigurableIssues
import com.mgtriffid.games.panna.shared.game.ConfigurableNetworkConditions
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.PannaGame
import com.mgtriffid.games.panna.shared.game.components.CharacterInputComponent2
import mu.KotlinLogging
import kotlin.math.max
import kotlin.math.min

const val SCALE = 3

private val logger = KotlinLogging.logger {}

// TODO handle resize and pause and all the things
class GameScreen(
    private val gdxGame: PannaGdxGame
) : ScreenAdapter() {
    private val drawableComponents = listOf(
        DrawableComponent::class,
        PositionComponent::class
    )

    private val graphics: GraphicsV2 = GraphicsV2()
    private lateinit var gdxAdapter: CottaGdxAdapter

    private lateinit var input: PannaClientGdxInput
    private lateinit var networkConditions: ConfigurableNetworkConditions

    override fun show() {
        val game = PannaGame()
        networkConditions = game.config.debugConfig.emulatedNetworkConditions as ConfigurableNetworkConditions
        graphics.initialize(networkConditions)
        input = PannaClientGdxInput(graphics.viewport)
        gdxAdapter = CottaGdxAdapter(game, input)
        gdxAdapter.initialize()
    }

    /**
     * This is called rapidly by LibGDX game loop. Think of this as of the main loop body.
     */
    override fun render(delta: Float) {
        logger.trace { "${GameScreen::class.simpleName}#render called" }
        updateNetworkConditions()
        draw(gdxAdapter(), delta)
    }

    private fun updateNetworkConditions() {
        updateNetworkConditions(networkConditions.sending, NetworkConditionsKeys.sending)
        updateNetworkConditions(networkConditions.receiving, NetworkConditionsKeys.receiving)
    }

    private fun updateNetworkConditions(
        conditions: ConfigurableIssues,
        keys: NetworkConditionsKeys.ConditionsKeys
    ) {
        if (Gdx.input.isKeyJustPressed(keys.decreaseMinLatency)) {
            conditions.latency.min = max(0, conditions.latency.min - 5)
        }
        if (Gdx.input.isKeyJustPressed(keys.increaseMinLatency)) {
            conditions.latency.min += 5
            conditions.latency.max = max(conditions.latency.min, conditions.latency.max)
        }
        if (Gdx.input.isKeyJustPressed(keys.decreaseMaxLatency)) {
            conditions.latency.max = max(0, conditions.latency.max - 5)
            conditions.latency.min = min(conditions.latency.min, conditions.latency.max)
        }
        if (Gdx.input.isKeyJustPressed(keys.increaseMaxLatency)) {
            conditions.latency.max += 5
        }
        if (Gdx.input.isKeyJustPressed(keys.decreasePacketLoss)) {
            conditions.packetLoss = max(0.0, conditions.packetLoss - 0.05)
        }
        if (Gdx.input.isKeyJustPressed(keys.increasePacketLoss)) {
            conditions.packetLoss = min(1.0, conditions.packetLoss + 0.05)
        }
    }

    override fun dispose() {
        graphics.dispose()
    }

    private fun draw(
        alphas: InterpolationAlphas,
        delta: Float
    ) {
        when (val state = getDrawableState(alphas)) {
            DrawableState.NotReady -> return
            is DrawableState.Ready -> {
                if (noDude(state)) {
                    input.mayJoin = true
                }
                graphics.draw(state, delta, input.mayJoin, gdxAdapter.metrics())
            }
        }
    }

    private fun noDude(state: DrawableState.Ready): Boolean {
        return state.entities.none {
            it.hasComponent(CharacterInputComponent2::class) &&
                it.ownedBy == Entity.OwnedBy.Player(state.playerId)
        }
    }

    private fun getDrawableState(alphas: InterpolationAlphas): DrawableState {
        return gdxAdapter.getDrawableState(alphas, drawableComponents)
    }
}
