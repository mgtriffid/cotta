package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.input.PlayerInput
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ServerIncomingDataBuffer {
    val inputs = TreeMap<Long, PlayerInput>()

    fun storeInput(tick: Long, input: PlayerInput) {
        inputs[tick] = input
        logger.debug { "Storing input for tick $tick : $input" }
        cleanUpOldInputs(tick)
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

    fun hasEnoughInputsToStart(): Boolean {
        if (inputs.isEmpty()) return false
        val lastInput = inputs.lastKey()
        return (0 until REQUIRED_CLIENT_INPUTS_BUFFER).all { inputs.containsKey(lastInput - it) }
    }
}
