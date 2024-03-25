package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.serialization.*
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

const val REQUIRED_CLIENT_INPUTS_BUFFER = 3

class ServerSimulationInputProviderImpl<
    SR : StateRecipe,
    DR : DeltaRecipe,
    IR : InputRecipe,
    CEWTR : CreatedEntitiesWithTracesRecipe,
    MEDR : MetaEntitiesDeltaRecipe
    > @Inject constructor(
    private val nonPlayerInputProvider: NonPlayerInputProvider,
    @Named("simulation") private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder,
    private val tickProvider: TickProvider,
    private val networkTransport: CottaServerNetworkTransport,
    private val inputSerialization: InputSerialization<IR>,
    private val stateSnapper: StateSnapper<SR, DR, CEWTR, MEDR>,
    private val snapsSerialization: SnapsSerialization<SR, DR, CEWTR, MEDR>,
    private val clientsGhosts: ClientsGhosts<IR>,
) : ServerSimulationInputProvider {
    private val buffers =
        HashMap<PlayerId, ServerIncomingDataBuffer<SR, DR, IR>>()

    override fun getDelta(): ServerDelta {
        val clientsInput = getInput()

        val nonPlayerInput =
            nonPlayerInputProvider.input(state.entities(tickProvider.tick))

        val input = object : SimulationInput {
            // TODO protect against malicious client sending input for entity not belonging to them
            override fun nonPlayerInput(): NonPlayerInput = nonPlayerInput

            override fun inputForPlayers() = clientsInput.inputForPlayers

            override fun playersSawTicks() = clientsInput.playersSawTicks
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
            val recipe = inputSerialization.deserializeInputRecipe(dto.payload)
            getBuffer(playerId).storeInput(dto.tick, recipe)
        }

        val inputDtos2 = networkTransport.drainInputs2()
        inputDtos2.forEach { (connectionId, dto) ->
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId == null) {
                logger.warn { "Got input from unknown connection $connectionId" }
                return@forEach
            }
            val input = inputSerialization.deserializeInput(dto.payload)
            getBuffer(playerId).storeInput2(dto.tick, input)
        }
    }

    private fun getInput(): ClientsInput {
        val playersSawTicks = HashMap<PlayerId, Long>()
        val playerInputs = HashMap<PlayerId, PlayerInput>()

        val use: (PlayerId, ClientGhost, Long) -> Unit =
            { playerId, ghost, tick ->
                val buffer = getBuffer(playerId)
                playersSawTicks[playerId] = tick
                playerInputs[playerId] = buffer.inputs2[tick]!!
                ghost.setLastUsedTick(tick)
                ghost.setLastUsedIncomingInput(buffer.inputs2[tick]!!)
            }
        val usePrevious: (PlayerId, ClientGhost, Long) -> Unit =
            { playerId, ghost, tick ->
                playersSawTicks[playerId] = tick
                playerInputs[playerId] = ghost.getLastUsedIncomingInput()
                ghost.setLastUsedTick(tick)
            }
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val buffer = getBuffer(playerId)
            when (ghost.tickCursorState()) {
                AWAITING_INPUTS -> {
                    if (buffer.hasEnoughInputsToStart()) {
                        ghost.setCursorState(RUNNING)
                        val tick =
                            buffer.inputs2.lastKey() - REQUIRED_CLIENT_INPUTS_BUFFER + 1
                        use(playerId, ghost, tick)
                    } else {
                        // do nothing. Ok, we don't have the input, no big deal.
                    }
                }

                RUNNING -> {
                    val lastUsedInput = ghost.lastUsedInput()
                    val tick = lastUsedInput + 1
                    logger.debug { "Client input tick is $tick for $playerId" }
                    if (
                        buffer.inputs2.containsKey(tick)
                    ) {
                        use(playerId, ghost, tick)
                    } else {
                        usePrevious(playerId, ghost, tick)
                    }
                }
            }
        }

        return ClientsInput(
            playersSawTicks = playersSawTicks,
            inputForPlayers = playerInputs
        )
    }

    private fun getBuffer(playerId: PlayerId): ServerIncomingDataBuffer<SR, DR, IR> {
        return buffers.computeIfAbsent(playerId) { ServerIncomingDataBuffer() }
    }
}
