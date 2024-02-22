package com.mgtriffid.games.cotta.gdx

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.utils.now
import kotlin.reflect.KClass

private val logger: mu.KLogger = mu.KotlinLogging.logger {}

class CottaGdxAdapter(
    private val game: CottaGame,
    private val input: CottaClientGdxInput
) {
    private lateinit var cottaClient: CottaClient

    private var nextTickAt: Long = -1
    private var tickLength: Long = -1

    fun initialize() {
        logger.debug { "Tick length is ${game.config.tickLength}" }
        tickLength = game.config.tickLength
        cottaClient = CottaClientFactory().create(game, input)
        nextTickAt = now()
    }

    operator fun invoke() : Float {
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

        return 1.0f - (nextTickAt - now).toFloat() / tickLength.toFloat()
    }

    fun getDrawableState(alpha: Float, components: List<KClass<out Component<*>>>): DrawableState {
        return cottaClient.getDrawableState(alpha, *components.toTypedArray())
    }
}
