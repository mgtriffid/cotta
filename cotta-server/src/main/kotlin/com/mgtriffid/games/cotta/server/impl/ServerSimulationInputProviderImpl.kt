package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.id.StaticEntityId
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.server.*
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.AWAITING_INPUTS
import com.mgtriffid.games.cotta.server.impl.ClientGhost.ClientTickCursor.State.RUNNING
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

const val REQUIRED_CLIENT_INPUTS_BUFFER = 3

class ServerSimulationInputProviderImpl @Inject constructor(
    private val nonPlayerInputProvider: NonPlayerInputProvider,
    @Named("simulation") private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder,
    private val predictedToAuthoritativeIdMappings: PredictedToAuthoritativeIdMappings,
    private val tickProvider: TickProvider,
    private val networkTransport: CottaServerNetworkTransport,
    private val inputSerialization: InputSerialization<MapsInputRecipe>,
    private val inputSnapper: InputSnapper<MapsInputRecipe>,
    private val stateSnapper: StateSnapper<MapsStateRecipe, MapsDeltaRecipe>,
    private val snapsSerialization: SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe>,
    private val clientsGhosts: ClientsGhosts,
) : ServerSimulationInputProvider {
    private val buffers = HashMap<PlayerId, ServerIncomingDataBuffer>()

    override fun getDelta(): ServerDelta {
        val (clientsInput, predictedClientEntities) = getInput()

        val nonPlayerInput = nonPlayerInputProvider.input(state.entities(tickProvider.tick))

        val remappedInput = clientsInput.input.mapKeys { (entityId, _) ->
            when (entityId) {
                is PredictedEntityId -> {
                    val authoritativeId = predictedToAuthoritativeIdMappings[entityId]
                    if (authoritativeId != null) {
                        logger.trace { "Remapping input for entity $entityId to $authoritativeId" }
                        authoritativeId
                    } else {
                        // TODO probably warning or not needed in the resulting map at all
                        logger.warn { "Not remapping input for entity $entityId" }
                        entityId
                    }
                }

                is AuthoritativeEntityId -> {
                    logger.trace { "Not remapping input for entity $entityId" }
                    entityId
                }

                is StaticEntityId -> throw IllegalStateException("Static entity $entityId cannot be controlled")
            }
        }

        val inputs = remappedInput + nonPlayerInput

        val input = object : SimulationInput {
            // TODO protect against malicious client sending input for entity not belonging to them
            override fun inputsForEntities() = inputs

            override fun playersSawTicks() = clientsInput.playersSawTicks
        }

        simulationInputHolder.set(input)

        return ServerDelta(
            input = input,
            createdEntities = predictedClientEntities
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

        val rawCreatedEntitiesDtos = networkTransport.drainCreatedEntities()
        rawCreatedEntitiesDtos.forEach { (connectionId, dto) ->
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId == null) {
                logger.warn { "Got input from unknown connection $connectionId" }
                return@forEach
            }
            val createdEntitiesRecipe = snapsSerialization.deserializeEntityCreationTraces(dto.payload)
            val createdEntities: List<Pair<CottaTrace, PredictedEntityId>> =
                createdEntitiesRecipe.map { (trace, entityId) ->
                    Pair(stateSnapper.unpackTrace(trace), entityId as PredictedEntityId)
                }
            getBuffer(playerId).storeCreatedEntities(dto.tick, createdEntities)
        }
    }

    private fun getInput(): Pair<ClientsInput, ClientsPredictedEntities> {
        val inputRecipes = ArrayList<MapsInputRecipe>()
        val createdEntities = ArrayList<Pair<CottaTrace, PredictedEntityId>>()
        val playersSawTicks = HashMap<PlayerId, Long>()

        val use: (PlayerId, ClientGhost, Long) -> Unit = { playerId, ghost, tick ->
            val buffer = getBuffer(playerId)
            playersSawTicks[playerId] = tick
            inputRecipes.add(buffer.inputs[tick]!!)
            createdEntities.addAll(buffer.createdEntities[tick + 1]!!)
            ghost.setLastUsedInput(tick)
        }
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val buffer = getBuffer(playerId)
            when (ghost.tickCursorState()) {
                AWAITING_INPUTS -> {
                    if (buffer.hasEnoughInputsToStart()) {
                        ghost.setCursorState(RUNNING)
                        val tick = buffer.inputs.lastKey() - REQUIRED_CLIENT_INPUTS_BUFFER + 1
                        use(playerId, ghost, tick)
                    } else {
                        // do nothing. Ok, we don't have the input recipe yet, no big deal.
                    }
                }

                RUNNING -> {
                    val lastUsedInput = ghost.lastUsedInput()
                    val tick = lastUsedInput + 1
                    logger.debug { "Client input tick is $tick for $playerId" }
                    if (buffer.inputs.containsKey(tick) && buffer.createdEntities.containsKey(tick + 1)) {
                        use(playerId, ghost, tick)
                    } else {
                        TODO("Not supported yet, need to take care of this legit missing packets case")
                    }
                }
            }
        }

        val inputs = inputRecipes.map { recipe ->
            inputSnapper.unpackInputRecipe(recipe).entries
        }.flatten().associate { it.key to it.value }
        return Pair(ClientsInput(playersSawTicks, inputs), ClientsPredictedEntities(createdEntities))
    }

    private fun getBuffer(playerId: PlayerId): ServerIncomingDataBuffer {
        return buffers.computeIfAbsent(playerId) { ServerIncomingDataBuffer() }
    }
}
