package com.mgtriffid.games.cotta.gdx

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.client.InterpolationAlphas
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.utils.now
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

    operator fun invoke() : InterpolationAlphas {
        input.accumulate()

        return client.update(now()).let { when (it) {
            is com.mgtriffid.games.cotta.client.UpdateResult.Running -> it.alphas
            else -> emptyInterpolationAlphas
        } }.also {
            logger.info { "Alpha for drawable state is $it" }
        }
    }

    fun getDrawableState(alphas: InterpolationAlphas, components: List<KClass<out Component<*>>>): DrawableState {
        return client.getDrawableState(alphas, *components.toTypedArray())
    }

    fun metrics() = client.debugMetrics
}

private val emptyInterpolationAlphas = InterpolationAlphas(0.0f, 0.0f)
