package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ServerIncomingDataBuffer {
    val inputs2 = TreeMap<ClientInputId, Pair<PlayerInput, Long>>()

    fun storeInput2(id: ClientInputId, input: PlayerInput, sawTick: Long) {
        inputs2[id] = Pair(input, sawTick)
        logger.debug { "Storing input for tick ${id.id} : $input" }
        cleanUpOldInputs2(id)
    }

    private fun cleanUpOldInputs2(id: ClientInputId) {
        inputs2.navigableKeySet().subSet(
            ClientInputId(min(inputs2.firstKey().id, id.id - 128)),
            ClientInputId(id.id - 128)
        ).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            inputs2.remove(it)
        }
    }

    fun hasEnoughInputsToStart(): Boolean {
        if (inputs2.isEmpty()) return false
        val lastInput = inputs2.lastKey()
        return (0 until REQUIRED_CLIENT_INPUTS_BUFFER).all {
            inputs2.containsKey(
                ClientInputId(lastInput.id - it)
            )
        }
    }
}
