package com.mgtriffid.games.cotta.client.network.impl

import com.mgtriffid.games.cotta.client.impl.AuthoritativeState
import com.mgtriffid.games.cotta.client.impl.ClientIncomingDataBuffer
import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.input.ClientInput
import com.mgtriffid.games.cotta.core.serialization.CreatedEntitiesWithTracesRecipe
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.protocol.ClientToServerCreatedPredictedEntitiesDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import jakarta.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NetworkClientImpl<
    SR: StateRecipe,
    DR: DeltaRecipe,
    IR: InputRecipe,
    CEWTR: CreatedEntitiesWithTracesRecipe
    > @Inject constructor(
    private val networkTransport: CottaClientNetworkTransport,
    private val incomingDataBuffer: ClientIncomingDataBuffer<SR, DR, IR, CEWTR>,
    private val snapsSerialization: SnapsSerialization<SR, DR, CEWTR>,
    private val inputSerialization: InputSerialization<IR>,
    private val inputSnapper: InputSnapper<IR>,
    private val stateSnapper: StateSnapper<SR, DR, CEWTR>,
    private val localPlayer: LocalPlayer
) : NetworkClient {
    private val lagCompLimit: Int = 8 // TODO move to config and bind properly
    private val bufferLength: Int = 3
    override fun connect() {
        networkTransport.initialize()
        networkTransport.sendEnterGameIntent()
    }

    override fun fetch() {
        val data = networkTransport.drainIncomingData()
        data.forEach {
            when (it.kindOfData) {
                KindOfData.DELTA -> incomingDataBuffer.storeDelta(
                    it.tick,
                    snapsSerialization.deserializeDeltaRecipe(it.payload)
                )

                KindOfData.STATE -> incomingDataBuffer.storeState(
                    it.tick,
                    snapsSerialization.deserializeStateRecipe(it.payload)
                )

                KindOfData.CLIENT_META_ENTITY_ID -> {
                    val (entityId, playerId) = snapsSerialization.deserializeMetaEntityId(it.payload)
                    localPlayer.set(entityId, playerId)
                }

                KindOfData.INPUT -> incomingDataBuffer.storeInput(
                    it.tick,
                    inputSnapper.unpackInputRecipe(inputSerialization.deserializeInputRecipe(it.payload))
                )

                KindOfData.PLAYERS_SAW_TICKS -> incomingDataBuffer.storePlayersSawTicks(
                    it.tick,
                    snapsSerialization.deserializePlayersSawTicks(it.payload)
                )

                KindOfData.CREATED_ENTITIES_V2 -> incomingDataBuffer.storeCreatedEntities(
                    it.tick,
                    snapsSerialization.deserializeEntityCreationTracesV2(it.payload) as CEWTR
                )

                null -> throw IllegalStateException("kindOfData is null in an incoming ServerToClientDto")
            }
        }
    }

    override fun send(inputs: ClientInput, tick: Long) {
        val inputRecipe = inputSnapper.snapInput(inputs.inputs)
        val inputDto = ClientToServerInputDto()
        inputDto.tick = tick
        inputDto.payload = inputSerialization.serializeInputRecipe(inputRecipe)
        networkTransport.sendInput(inputDto)
    }

    override fun send(createdEntities: List<Pair<CottaTrace, EntityId>>, tick: Long) {
        val createdEntitiesRecipe = createdEntities.map { (trace, id) ->
            Pair(stateSnapper.snapTrace(trace), id)
        }
        val createdEntitiesDto = ClientToServerCreatedPredictedEntitiesDto()
        createdEntitiesDto.tick = tick
        createdEntitiesDto.payload = snapsSerialization.serializeEntityCreationTraces(createdEntitiesRecipe)
        networkTransport.sendCreatedEntities(createdEntitiesDto)
    }

    override fun tryGetDelta(tick: Long): Delta = if (deltaAvailable(tick)) {
        val input = incomingDataBuffer.inputs[tick]!!
        Delta.Present(
            applyDiff = { entities ->
                applyDelta(entities, tick)
            },
            input = object : SimulationInput {
                override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                    return input
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return incomingDataBuffer.playersSawTicks[tick]!!
                }
            },
            authoritativeToPredictedEntities = incomingDataBuffer.createdEntities[tick + 1]!!.mappedPredictedIds,
            tracesOfCreatedEntities = incomingDataBuffer.createdEntities[tick + 1]!!.traces.map { (traceRecipe, entityId) ->
                Pair(stateSnapper.unpackTrace(traceRecipe), entityId)
            }
        )
    } else {
        Delta.Absent
    }

    override fun tryGetAuthoritativeState(): AuthoritativeState {
        if (!localPlayer.isReady()) {
            return AuthoritativeState.NotReady
        }
        return if (stateAvailable()) {
            AuthoritativeState.Ready { state, tickProvider ->
                logger.debug { "Setting state from authoritative" }
                val fullStateTick = incomingDataBuffer.states.lastKey()
                val stateRecipe = incomingDataBuffer.states[fullStateTick]!!
                state.setBlank(fullStateTick)
                tickProvider.tick = fullStateTick
                stateSnapper.unpackStateRecipe(state.entities(atTick = fullStateTick), stateRecipe)
                ((fullStateTick + 1)..(fullStateTick + lagCompLimit)).forEach { tick ->
                    state.advance(tick - 1)
                    tickProvider.tick++
                    applyDelta(state, tick)
                }
            }
        } else {
            AuthoritativeState.NotReady
        }
    }

    private fun applyDelta(state: CottaState, tick: Long) {
        applyDelta(state.entities(atTick = tick), tick - 1)
    }

    private fun applyDelta(entities: Entities, tick: Long) {
        stateSnapper.unpackDeltaRecipe(entities, incomingDataBuffer.deltas[tick]!!)
    }

    private fun stateAvailable(): Boolean {
        val stateArrived = incomingDataBuffer.states.isNotEmpty()
        if (!stateArrived) return false
        val stateTick = incomingDataBuffer.states.lastKey()
        val deltasForLagCompArrived =
            incomingDataBuffer.deltas.keys.containsAll(((stateTick + 1)..(stateTick + lagCompLimit + bufferLength)).toList())
        return deltasForLagCompArrived
    }

    private fun deltaAvailable(tick: Long): Boolean {
        return incomingDataBuffer.deltas.containsKey(tick).also { logger.debug { "Delta present for tick $tick: $it" } }
            && incomingDataBuffer.inputs.containsKey(tick).also { logger.debug { "Input present for tick $tick: $it" } }
            && incomingDataBuffer.playersSawTicks.containsKey(tick).also { logger.debug { "sawTicks present for tick $tick: $it" } }
            && incomingDataBuffer.createdEntities.containsKey(tick).also { logger.debug { "createEntities present for tick $tick: $it" } }
    }
}
