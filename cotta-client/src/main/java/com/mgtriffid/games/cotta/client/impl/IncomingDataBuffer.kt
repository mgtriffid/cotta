package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class IncomingDataBuffer<SR: StateRecipe, DR: DeltaRecipe> {
    val states = TreeMap<Long, SR>()
    val deltas = TreeMap<Long, DR>()

    fun storeDelta(tick: Long, delta: DR) {
        deltas[tick] = delta
        cleanUpOldDeltas(tick)
    }

    private fun cleanUpOldDeltas(tick: Long) {
        logger.debug { "Cleaning deltas" }
        cleanUp(deltas, tick)
        logger.debug { "Cleaning deltas completed" }
    }

    fun storeState(tick: Long, state: SR) {
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
