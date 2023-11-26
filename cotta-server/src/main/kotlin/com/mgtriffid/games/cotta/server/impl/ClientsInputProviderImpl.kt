package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.ClientsInputProvider
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.AWAITING_INPUTS
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.RUNNING
import jakarta.inject.Inject
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

const val REQUIRED_CLIENT_INPUTS_BUFFER = 3

class ClientsInputProviderImpl<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> @Inject constructor(
    val network: CottaServerNetwork,
    val inputSerialization: InputSerialization<IR>,
    val inputSnapper: InputSnapper<IR>,
    val stateSnapper: StateSnapper<SR, DR>,
    val snapsSerialization: SnapsSerialization<SR, DR>,
    val clientsGhosts: ClientsGhosts,
) : ClientsInputProvider {
    private val inputBuffers = HashMap<PlayerId, ClientDataBuffer<IR>>()
    private val createdEntitiesBuffer = HashMap<PlayerId, ClientDataBuffer<List<Pair<CottaTrace, EntityId>>>>()
    override fun getInput(): ClientsInput {
        val rawDtos = network.drainInputs()
        rawDtos.forEach { (connectionId, dto) ->
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId == null) {
                logger.warn { "Got input from unknown connection $connectionId" }
                return@forEach
            }
            val recipe = inputSerialization.deserializeInputRecipe(dto.payload)
            getInputBuffer(playerId).store(dto.tick, recipe)
        }
        val rawCreatedEntitiesDtos = network.drainCreatedEntities()

        rawCreatedEntitiesDtos.forEach { (connectionId, dto) ->
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId == null) {
                logger.warn { "Got input from unknown connection $connectionId" }
                return@forEach
            }
            val createdEntitiesRecipe = snapsSerialization.deserializeEntityCreationTraces(dto.payload)
            val createdEntities: List<Pair<CottaTrace, EntityId>> = createdEntitiesRecipe.map { (trace, entityId) ->
                Pair(stateSnapper.unpackTrace(trace), entityId)
            }
            getCreatedEntitiesBuffer(playerId).store(dto.tick, createdEntities)
        }

        // now we decide which input to take for which client
        // cases:
        // - inputs are ready client ghost is awaiting buffered input
        // - inputs are not ready client ghost is awaiting buffered input
        // - inputs are ready client ghost is running
        // - inputs are not ready client ghost is running
        val inputRecipes = ArrayList<IR>()
        val createdEntities = ArrayList<Pair<CottaTrace, EntityId>>()
        val playersSawTicks = HashMap<PlayerId, Long>()
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val tickCursorState = ghost.tickCursorState()
            val inputBuffer = getInputBuffer(playerId)
            when (tickCursorState) {
                AWAITING_INPUTS -> {
                    if (inputBuffer.hasEnoughInputsToStart()) {
                        ghost.setCursorState(RUNNING)
                        val clientsTickToUse = inputBuffer.lastKey() - REQUIRED_CLIENT_INPUTS_BUFFER + 1
                        ghost.setLastUsedInput(clientsTickToUse)
                        playersSawTicks[playerId] = clientsTickToUse
                        inputRecipes.add(inputBuffer.get(clientsTickToUse))
                        createdEntities.addAll(getCreatedEntitiesBuffer(playerId).get(clientsTickToUse))
                    } else {
                        // do nothing. Ok, we don't have the input recipe yet, no big deal.
                    }
                }
                RUNNING -> {
                    val lastUsedInput = ghost.lastUsedInput()
                    val clientsTickToUse = lastUsedInput + 1
                    logger.debug { "Client input tick is $clientsTickToUse for $playerId" }
                    if (inputBuffer.has(clientsTickToUse)) {
                        inputRecipes.add(inputBuffer.get(clientsTickToUse))
                        createdEntities.addAll(getCreatedEntitiesBuffer(playerId).get(clientsTickToUse))
                        ghost.setLastUsedInput(clientsTickToUse)
                        playersSawTicks[playerId] = clientsTickToUse
                    } else {
                        TODO("Not supported yet, need to take care of this legit missing packets case")
                    }
                }
            }
        }
        cleanOldInputs()
        cleanOldCreatedEntities()

        val inputs = inputRecipes.map { recipe ->
            inputSnapper.unpackInputRecipe(recipe).entries
        }.flatten().associate { it.key to it.value }
        return ClientsInput(playersSawTicks, inputs)
    }

    private fun getInputBuffer(playerId: PlayerId): ClientDataBuffer<IR> {
        return inputBuffers.computeIfAbsent(playerId) { ClientDataBuffer() }
    }

    private fun getCreatedEntitiesBuffer(playerId: PlayerId): ClientDataBuffer<List<Pair<CottaTrace, EntityId>>> {
        return createdEntitiesBuffer.computeIfAbsent(playerId) { ClientDataBuffer() }
    }

    private fun cleanOldInputs() {
        inputBuffers.values.forEach { it.cleanOld() }
    }

    private fun cleanOldCreatedEntities() {
        createdEntitiesBuffer.values.forEach { it.cleanOld() }
    }
}

// a ring-buffer bounded by 128 that stores input client would be better
private class ClientDataBuffer<T> {
    private val data = TreeMap<Long, T>()

    fun store(tick: Long, recipe: T) {
        data[tick] = recipe
    }

    fun get(tick: Long) : T {
        return data[tick]!!
    }

    fun has(tick: Long) = data.containsKey(tick)

    fun lastKey() = data.lastKey()!!

    fun hasEnoughInputsToStart(): Boolean {
        if (data.isEmpty()) return false
        val lastInput = data.lastKey()
        return (0 until REQUIRED_CLIENT_INPUTS_BUFFER).all { has(lastInput - it) }
    }

    fun cleanOld() {
        if (data.isEmpty()) return
        val lastKey = data.lastKey()
        val firstKey = lastKey - 128
        data.headMap(firstKey).clear()
    }
}
