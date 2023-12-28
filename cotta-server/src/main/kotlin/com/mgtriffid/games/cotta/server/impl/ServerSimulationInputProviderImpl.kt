package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.server.*
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
    private val incomingDataBuffers = HashMap<PlayerId, ServerIncomingDataBuffer>()

    // TODO naming. For now this is called delta for similarity with client side
    override fun getDelta(): ServerDelta {
        val dataFromClients = getInput()
        val clientsInput = dataFromClients.first
        val predictedClientEntities = dataFromClients.second

        val nonPlayerEntitiesInput = nonPlayerInputProvider.input(state.entities(tickProvider.tick))

        val remappedInput = clientsInput.input.entries.associate { (eId, components) ->
            when (eId) {
                is PredictedEntityId -> {
                    val authoritativeId = predictedToAuthoritativeIdMappings[eId]
                    if (authoritativeId != null) {
                        logger.trace { "Remapping input for entity $eId to $authoritativeId" }
                        authoritativeId to components
                    } else {
                        // TODO probably warning or not needed in the resulting map at all
                        logger.trace { "Not remapping input for entity $eId" }
                        eId to components
                    }
                }

                is AuthoritativeEntityId -> {
                    logger.trace { "Not remapping input for entity $eId" }
                    eId to components
                }
            }
        }

        val inputs = remappedInput + nonPlayerEntitiesInput

        logger.trace { "Incoming input has ${inputs.size} entries" }

        inputs.forEach { (eId, components) ->
            logger.trace { "Inputs for entity $eId:" }
            components.forEach { logger.trace { it } }
        }
        val input = object : SimulationInput {
            // TODO protect against malicious client sending input for entity not belonging to them
            override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                return inputs
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return clientsInput.playersSawTicks
            }
        }

        simulationInputHolder.set(input)

        return ServerDelta(
            input = input,
            createdEntities = predictedClientEntities
        )
    }

    override fun fetch() {
        val rawDtos = networkTransport.drainInputs()
        rawDtos.forEach { (connectionId, dto) ->
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId == null) {
                logger.warn { "Got input from unknown connection $connectionId" }
                return@forEach
            }
            val recipe = inputSerialization.deserializeInputRecipe(dto.payload)
            getIncomingDataBuffer(playerId).storeInput(dto.tick, recipe)
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
            getIncomingDataBuffer(playerId).storeCreatedEntities(dto.tick, createdEntities)
        }
    }

    private fun getInput(): Pair<ClientsInput, ClientsPredictedEntities> {


        // now we decide which input to take for which client
        // cases:
        // - inputs are ready client ghost is awaiting buffered input
        // - inputs are not ready client ghost is awaiting buffered input
        // - inputs are ready client ghost is running
        // - inputs are not ready client ghost is running
        val inputRecipes = ArrayList<MapsInputRecipe>()
        val createdEntities = ArrayList<Pair<CottaTrace, PredictedEntityId>>()
        val playersSawTicks = HashMap<PlayerId, Long>()
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val tickCursorState = ghost.tickCursorState()
            val incomingDataBuffer = getIncomingDataBuffer(playerId)
            when (tickCursorState) {
                ClientGhost.ClientTickCursor.State.AWAITING_INPUTS -> {
                    if (incomingDataBuffer.hasEnoughInputsToStart()) {
                        ghost.setCursorState(ClientGhost.ClientTickCursor.State.RUNNING)
                        val clientsTickToUse = incomingDataBuffer.inputs.lastKey() - REQUIRED_CLIENT_INPUTS_BUFFER + 1
                        ghost.setLastUsedInput(clientsTickToUse)
                        playersSawTicks[playerId] = clientsTickToUse
                        inputRecipes.add(incomingDataBuffer.inputs[clientsTickToUse]!!)
                        // TODO doesn't check if the created entities are there, need to check
                        createdEntities.addAll(incomingDataBuffer.createdEntities[clientsTickToUse + 1]!!)
                    } else {
                        // do nothing. Ok, we don't have the input recipe yet, no big deal.
                    }
                }

                ClientGhost.ClientTickCursor.State.RUNNING -> {
                    val lastUsedInput = ghost.lastUsedInput()
                    val clientsTickToUse = lastUsedInput + 1
                    logger.debug { "Client input tick is $clientsTickToUse for $playerId" }
                    if (incomingDataBuffer.inputs.containsKey(clientsTickToUse)) {
                        inputRecipes.add(incomingDataBuffer.inputs[clientsTickToUse]!!)
                        // TODO doesn't check if the created entities are there, need to check
                        createdEntities.addAll(incomingDataBuffer.createdEntities[clientsTickToUse + 1]!!)
                        ghost.setLastUsedInput(clientsTickToUse)
                        playersSawTicks[playerId] = clientsTickToUse
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

    private fun getIncomingDataBuffer(playerId: PlayerId): ServerIncomingDataBuffer {
        return incomingDataBuffers.computeIfAbsent(playerId) { ServerIncomingDataBuffer() }
    }
}
