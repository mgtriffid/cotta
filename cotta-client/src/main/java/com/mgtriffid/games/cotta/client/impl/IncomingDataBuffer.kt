package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class IncomingDataBuffer<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> {
    val states = TreeMap<Long, SR>()
    val deltas = TreeMap<Long, DR>()
    val inputs = TreeMap<Long, IR>()
    val metaEntityIds = TreeMap<Long, EntityId>() // not really needed but for the uniformity

    fun storeDelta(tick: Long, delta: DR) {
        deltas[tick] = delta
        cleanUpOldDeltas(tick)
    }

    private fun cleanUpOldDeltas(tick: Long) {
        cleanUp(deltas, tick)
    }

    fun storeState(tick: Long, state: SR) {
        states[tick] = state
        cleanUpOldStates(tick)
    }

    fun storeInput(tick: Long, input: IR) {
        inputs[tick] = input
        cleanUpOldInputs(tick)
    }

    private fun cleanUpOldStates(tick: Long) {
        cleanUp(states, tick)
    }

    private fun cleanUpOldInputs(tick: Long) {
        cleanUp(inputs, tick)
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }
}
