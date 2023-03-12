package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class IncomingDataBuffer {
    private val states = TreeMap<Long, StateRecipe>()
    private val deltas = TreeMap<Long, DeltaRecipe>()

    fun storeDelta(tick: Long, delta: DeltaRecipe) {
        deltas[tick] = delta
        cleanUpOldDeltas(tick)
    }

    private fun cleanUpOldDeltas(tick: Long) {
        logger.debug { "Cleaning deltas" }
        cleanUp(deltas, tick)
        logger.debug { "Cleaning deltas completed" }
    }

    fun storeState(tick: Long, state: StateRecipe) {
        states[tick] = state
        cleanUpOldStates(tick)
    }

    private fun cleanUpOldStates(tick: Long) {
        logger.debug { "Cleaning states" }
        cleanUp(states, tick)
        logger.debug { "Cleaning states completed" }
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }
}
