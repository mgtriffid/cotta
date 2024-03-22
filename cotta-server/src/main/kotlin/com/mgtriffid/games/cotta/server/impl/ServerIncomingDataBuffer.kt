package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ServerIncomingDataBuffer<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> {
    val inputs = TreeMap<Long, IR>()
    val inputs2 = TreeMap<Long, PlayerInput>()
    val createdEntities = TreeMap<Long, List<Pair<CottaTrace, PredictedEntityId>>>() // GROOM class with naming

    fun storeInput(tick: Long, recipe: IR) {
        inputs[tick] = recipe
        cleanUpOldInputs(tick)
    }

    fun storeInput2(tick: Long, input: PlayerInput) {
        inputs2[tick] = input
        logger.debug { "Storing input for tick $tick : $input" }
        cleanUpOldInputs2(tick)
    }

    fun storeCreatedEntities(tick: Long, createdEntities: List<Pair<CottaTrace, PredictedEntityId>>) {
        this.createdEntities[tick] = createdEntities
        cleanUpOldCreatedEntities(tick)
    }

    private fun cleanUpOldInputs(tick: Long) {
        cleanUp(inputs, tick)
    }

    private fun cleanUpOldInputs2(tick: Long) {
        cleanUp(inputs2, tick)
    }

    private fun cleanUpOldCreatedEntities(tick: Long) {
        cleanUp(createdEntities, tick)
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }

    fun hasEnoughInputsToStart(): Boolean {
        if (inputs2.isEmpty()) return false
        val lastInput = inputs2.lastKey()
        return (0 until REQUIRED_CLIENT_INPUTS_BUFFER).all { inputs2.containsKey(lastInput - it) }
    }
}
