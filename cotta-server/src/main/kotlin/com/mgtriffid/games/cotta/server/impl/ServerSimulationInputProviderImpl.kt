package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.server.ServerDelta
import com.mgtriffid.games.cotta.server.ServerSimulationInputProvider
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.AWAITING_INPUTS
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.RUNNING
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// TODO make this configurable
const val REQUIRED_CLIENT_INPUTS_BUFFER = 3

// TODO remove this parameter, we're not going to have different serialization
//  mechanisms
class ServerSimulationInputProviderImpl<
    IR : InputRecipe
    > @Inject constructor(
    private val nonPlayerInputProvider: NonPlayerInputProvider,
    @Named("simulation") private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder,
    @Named(SIMULATION) private val tickProvider: TickProvider,
    private val networkTransport: CottaServerNetworkTransport,
    private val inputSerialization: InputSerialization<IR>,
    private val clientsGhosts: ClientsGhosts<IR>,
) : ServerSimulationInputProvider {
    private val buffers =
        HashMap<PlayerId, ServerIncomingDataBuffer>()

    override fun getDelta(): ServerDelta {
        val clientsInput = getInput()

        val nonPlayerInput =
            nonPlayerInputProvider.input(state.entities(tickProvider.tick))

        val input = object : SimulationInput {
            // TODO protect against malicious client sending input for entity not belonging to them
            override fun nonPlayerInput(): NonPlayerInput = nonPlayerInput

            override fun inputForPlayers() = clientsInput.inputForPlayers

            override fun playersSawTicks() = clientsInput.playersSawTicks

            override fun playersDiff(): PlayersDiff { // TODO wrong place obv
                return PlayersDiff(emptySet())
            }
        }

        simulationInputHolder.set(input)

        return ServerDelta(
            input = input
        )
    }

    override fun fetch() {
        val inputDtos = networkTransport.drainInputs()
        inputDtos.forEach { (connectionId, dto) ->
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId == null) {
                logger.warn { "Got input from unknown connection $connectionId" }
                return@forEach
            }
            val input = inputSerialization.deserializeInput(dto.payload)
            getBuffer(playerId).storeInput2(ClientInputId(dto.inputId), input, dto.sawTick)
        }
    }

    private fun getInput(): ClientsInput {
        val playersSawTicks = HashMap<PlayerId, Long>()
        val playerInputs = HashMap<PlayerId, PlayerInput>()

        clientsGhosts.data.forEach { (playerId, ghost) ->
            val buffer = getBuffer(playerId)
            when (ghost.tickCursorState()) {
                AWAITING_INPUTS -> {
                    if (buffer.hasEnoughInputsToStart()) {
                        ghost.setCursorState(RUNNING)
                        val id = ClientInputId(
                            buffer.inputs2.lastKey().id - REQUIRED_CLIENT_INPUTS_BUFFER + 1
                        )
                        val (input, sawTick) = buffer.inputs2[id]!!
                        playersSawTicks[playerId] = sawTick
                        playerInputs[playerId] = input
                        ghost.setLastUsedInputId(id)
                        ghost.setLastUsedInput(input)
                        ghost.setInputLastUsedOnTick(tickProvider.tick)
                    } else {
                        // do nothing. Ok, we don't have the input, no big deal.
                    }
                }

                RUNNING -> {
                    val lastUsedInput = ghost.lastUsedInput()
                    val toUse = ClientInputId(lastUsedInput.id + 1)
                    logger.debug { "Client input tick is $toUse for $playerId" }
                    if (
                        buffer.inputs2.containsKey(toUse)
                    ) {
                        val (input, sawTick) = buffer.inputs2[toUse]!!
                        ghost.setLastPresentInputId(toUse)
                        playersSawTicks[playerId] = sawTick
                        playerInputs[playerId] = input
                        ghost.setLastUsedInputId(toUse)
                        ghost.setLastUsedInput(input)
                        ghost.setInputLastUsedOnTick(tickProvider.tick)
                    } else {
                        val input = ghost.getLastUsedIncomingInput()
                        val inputId = ghost.getLastPresentInputId()
                        ghost.setLastUsedInputId(toUse)
                        val sawTick = buffer.inputs2[inputId]!!.second + (tickProvider.tick - ghost.lastInputUsedOnTick)
                        playersSawTicks[playerId] = sawTick
                        playerInputs[playerId] = input
                    }
                }
            }
        }

        return ClientsInput(
            playersSawTicks = playersSawTicks,
            inputForPlayers = playerInputs
        )
    }

    private fun getBuffer(playerId: PlayerId): ServerIncomingDataBuffer {
        return buffers.computeIfAbsent(playerId) { ServerIncomingDataBuffer() }
    }

    override fun bufferAheadLength(playerId: PlayerId): Int {
        val ghost = clientsGhosts.data[playerId]
        if (ghost == null) {
            logger.warn { "Could not find a ghost for player ${playerId.id}, can't calculate buffer length" }
            return 0
        }
        val state = ghost.tickCursorState()
        if (state == AWAITING_INPUTS) {
            logger.debug { "Awaiting inputs for player ${playerId.id}" }
            return 0
        }
        val lastUsedInput = ghost.lastUsedInput()
        logger.debug { "Last used input : ${lastUsedInput.id}" }
        val buffer = getBuffer(playerId)
        var ret = 0
        logger.debug { "Buffer keys: ${buffer.inputs2.subMap(lastUsedInput, true, buffer.inputs2.lastKey(), true).keys.map { it.id }}" }
        while (buffer.inputs2.containsKey(ClientInputId(lastUsedInput.id + ret + 1))) {
            ret++
        }
        return ret.also { logger.debug { "Calculated Buffer length is $it" } }
    }
}
