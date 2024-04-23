package com.mgtriffid.games.cotta.gdx

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

private val logger: mu.KLogger = mu.KotlinLogging.logger {}

class CottaGdxAdapter(
    private val game: CottaGame,
    private val input: CottaClientGdxInput
) {
    private lateinit var client: CottaClient

    fun initialize() {
        logger.debug { "Tick length is ${game.config.tickLength}" }
        client = CottaClientFactory().create(game, input)
    }

    operator fun invoke() : Float {
        input.accumulate()

        return client.update().alpha
    }

    fun getDrawableState(alpha: Float, components: List<KClass<out Component<*>>>): DrawableState {
        return client.getDrawableState(alpha, *components.toTypedArray())
    }

    fun metrics() = client.debugMetrics
}
