package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.utils.RingBuffer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerIncomingDataBuffer {
    val inputs = RingBuffer<Pair<PlayerInput, Long>>(128)

    fun storeInput2(id: ClientInputId, input: PlayerInput, sawTick: Long) {
        inputs[id.id.toLong()] = Pair(input, sawTick)
        logger.debug { "Storing input for tick ${id.id} : $input" }
    }

    fun hasEnoughInputsToStart(): Boolean {
        if (inputs.isEmpty()) return false
        val lastInput = inputs.lastSet
        return (0 until REQUIRED_CLIENT_INPUTS_BUFFER).all {
            inputs[lastInput - it] != null
        }
    }
}
