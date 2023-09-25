package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.ClientsInputProvider
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.*
import mu.KotlinLogging
import java.util.*
import kotlin.collections.HashMap

private val logger = KotlinLogging.logger {}

const val REQUIRED_CLIENT_INPUTS_BUFFER = 3

class ClientsInputProviderImpl<IR: InputRecipe>(
    val network: CottaServerNetwork,
    val inputSerialization: InputSerialization<IR>,
    val inputSnapper: InputSnapper<IR>,
    val clientsGhosts: ClientsGhosts,
) : ClientsInputProvider {
    private val inputBuffers = HashMap<PlayerId, ClientInputBuffer<IR>>()

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
        // now we decide which input to take for which client
        // cases:
        // - inputs are ready client ghost is awaiting buffered input
        // - inputs are not ready client ghost is awaiting buffered input
        // - inputs are ready client ghost is running
        // - inputs are not ready client ghost is running
        val inputRecipes = ArrayList<IR>()
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
                        ghost.setLastUsedInput(clientsTickToUse)
                        playersSawTicks[playerId] = clientsTickToUse
                    } else {
                        TODO("Not supported yet, need to take care of this legit missing packets case")
                    }
                }
            }
        }
        cleanOldInputs()

        val inputs = inputRecipes.map { recipe ->
            inputSnapper.unpackInputRecipe(recipe).entries
        }.flatten().associate { it.key to it.value }
        return ClientsInput(playersSawTicks, inputs)
    }

    private fun getInputBuffer(playerId: PlayerId): ClientInputBuffer<IR> {
        return inputBuffers.computeIfAbsent(playerId) { ClientInputBuffer() }
    }

    private fun cleanOldInputs() {
        inputBuffers.values.forEach { it.cleanOld() }
    }
}

// a ring-buffer bounded by 128 that stores input client would be better
private class ClientInputBuffer<IR: InputRecipe> {
    private val data = TreeMap<Long, IR>()

    fun store(tick: Long, recipe: IR) {
        data[tick] = recipe
    }

    fun get(tick: Long) : IR {
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
