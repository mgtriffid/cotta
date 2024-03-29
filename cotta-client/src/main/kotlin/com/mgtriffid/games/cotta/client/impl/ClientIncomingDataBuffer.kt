package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.PlayersDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ClientIncomingDataBuffer<
    SR : StateRecipe,
    DR : DeltaRecipe,
    PDR: PlayersDeltaRecipe
    > {
    val states = TreeMap<Long, SR>()
    val deltas = TreeMap<Long, DR>()
    val playersDeltas = TreeMap<Long, PDR>()
    val inputs2 = TreeMap<Long, Map<PlayerId, PlayerInput>>() // GROOM class with naming
    val playersSawTicks = TreeMap<Long, Map<PlayerId, Long>>()

    fun storeDelta(tick: Long, delta: DR) {
        deltas[tick] = delta
        cleanUpOldDeltas(tick)
    }

    fun storeMetaEntitiesDelta(tick: Long, delta: PDR) {
        playersDeltas[tick] = delta
        cleanUpOldMetaEntitiesDelta(tick)
    }

    private fun cleanUpOldDeltas(tick: Long) {
        cleanUp(deltas, tick)
    }

    fun storeState(tick: Long, state: SR) {
        states[tick] = state
        cleanUpOldStates(tick)
    }

    fun storeInput2(tick: Long, input: Map<PlayerId, PlayerInput>) {
        logger.debug { "Storing input2 for $tick, data buffer ${this.hashCode()}" }
        inputs2[tick] = input
        cleanUpOldInputs2(tick)
    }

    fun storePlayersSawTicks(tick: Long, playersSawTicks: Map<PlayerId, Long>) {
        this.playersSawTicks[tick] = playersSawTicks
        cleanUpOldPlayersSawTicks(tick)
    }

    private fun cleanUpOldPlayersSawTicks(tick: Long) {
        cleanUp(playersSawTicks, tick)
    }

    private fun cleanUpOldMetaEntitiesDelta(tick: Long) {
        cleanUp(playersDeltas, tick)
    }

    private fun cleanUpOldStates(tick: Long) {
        cleanUp(states, tick)
    }

    private fun cleanUpOldInputs2(tick: Long) {
        cleanUp(inputs2, tick)
    }

    // GROOM same on server
    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }
}
