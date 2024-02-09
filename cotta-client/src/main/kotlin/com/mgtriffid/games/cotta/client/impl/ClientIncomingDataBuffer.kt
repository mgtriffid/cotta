package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsCreatedEntitiesWithTracesRecipe
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ClientIncomingDataBuffer<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> {
    val states = TreeMap<Long, SR>()
    val deltas = TreeMap<Long, DR>()
    val inputs = TreeMap<Long, Map<EntityId, Collection<InputComponent<*>>>>() // GROOM class with naming
    val createdEntities = TreeMap<Long, MapsCreatedEntitiesWithTracesRecipe>() // GROOM class with naming
    val playersSawTicks = TreeMap<Long, Map<PlayerId, Long>>()

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

    fun storeInput(tick: Long, input: Map<EntityId, Collection<InputComponent<*>>>) {
        logger.debug { "Storing input for $tick, data buffer ${this.hashCode()}" }
        inputs[tick] = input
        cleanUpOldInputs(tick)
    }

    fun storeCreatedEntities(tick: Long, createdEntities: MapsCreatedEntitiesWithTracesRecipe) {
        this.createdEntities[tick] = createdEntities
        cleanUpOldCreatedEntities(tick)
    }

    private fun cleanUpOldCreatedEntities(tick: Long) {
        cleanUp(createdEntities, tick)
    }

    fun storePlayersSawTicks(tick: Long, playersSawTicks: Map<PlayerId, Long>) {
        this.playersSawTicks[tick] = playersSawTicks
        cleanUpOldPlayersSawTicks(tick)
    }

    private fun cleanUpOldPlayersSawTicks(tick: Long) {
        cleanUp(playersSawTicks, tick)
    }

    private fun cleanUpOldStates(tick: Long) {
        cleanUp(states, tick)
    }

    private fun cleanUpOldInputs(tick: Long) {
        cleanUp(inputs, tick)
    }

    // GROOM same on server
    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }
}
