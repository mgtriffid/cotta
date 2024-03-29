package com.mgtriffid.games.cotta.client.network.impl

import com.mgtriffid.games.cotta.client.impl.AuthoritativeState
import com.mgtriffid.games.cotta.client.impl.ClientIncomingDataBuffer
import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.PlayersDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto2
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import jakarta.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NetworkClientImpl<
    SR: StateRecipe,
    DR: DeltaRecipe,
    IR: InputRecipe,
    PDR: PlayersDeltaRecipe
    > @Inject constructor(
    private val networkTransport: CottaClientNetworkTransport,
    private val incomingDataBuffer: ClientIncomingDataBuffer<SR, DR, PDR>,
    private val snapsSerialization: SnapsSerialization<SR, DR, PDR>,
    private val inputSerialization: InputSerialization<IR>,
    private val stateSnapper: StateSnapper<SR, DR, PDR>,
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

                KindOfData.PLAYERS_DELTA -> incomingDataBuffer.storeMetaEntitiesDelta(
                    it.tick,
                    snapsSerialization.deserializePlayersDeltaRecipe(it.payload)
                )

                KindOfData.STATE -> incomingDataBuffer.storeState(
                    it.tick,
                    snapsSerialization.deserializeStateRecipe(it.payload)
                )

                KindOfData.PLAYER_ID -> {
                    localPlayer.set(snapsSerialization.deserializePlayerId(it.payload))
                }

                KindOfData.INPUT2 -> incomingDataBuffer.storeInput2(
                    it.tick,
                    inputSerialization.deserializePlayersInputs(it.payload)
                )

                KindOfData.PLAYERS_SAW_TICKS -> incomingDataBuffer.storePlayersSawTicks(
                    it.tick,
                    snapsSerialization.deserializePlayersSawTicks(it.payload)
                )

                null -> throw IllegalStateException("kindOfData is null in an incoming ServerToClientDto")
            }
        }
    }

    override fun send(input: PlayerInput, currentTick: Long) {
        val inputDto = ClientToServerInputDto2()
        inputDto.tick = currentTick
        logger.debug { "Sending input for $currentTick : $input" }
        inputDto.payload = inputSerialization.serializeInput(input)
        networkTransport.send(inputDto)
    }

    override fun tryGetDelta(tick: Long): Delta? = if (deltaAvailable(tick)) {
        Delta(
            playersDiff = stateSnapper.unpackPlayersDeltaRecipe((incomingDataBuffer.playersDeltas[tick]!!)),
            input = object : SimulationInput {
                override fun nonPlayerInput(): NonPlayerInput {
                    return object : NonPlayerInput {}
                }

                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return incomingDataBuffer.inputs2[tick]!!.also { logger.debug { "In getting delta: $it" } }
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return incomingDataBuffer.playersSawTicks[tick]!!
                }
            },
        )
    } else {
        null
    }

    override fun tryGetAuthoritativeState(): AuthoritativeState {
        if (!localPlayer.isReady()) {
            return AuthoritativeState.NotReady
        }
        return if (stateAvailable()) {
            AuthoritativeState.Ready { state, simulationTickProvider, globalTickProvider ->
                logger.debug { "Setting state from authoritative" }
                val fullStateTick = incomingDataBuffer.states.lastKey()
                val stateRecipe = incomingDataBuffer.states[fullStateTick]!!
                state.setBlank(fullStateTick)
                simulationTickProvider.tick = fullStateTick
                globalTickProvider.tick = fullStateTick
                stateSnapper.unpackStateRecipe(state.entities(atTick = fullStateTick), stateRecipe)
                ((fullStateTick + 1)..(fullStateTick + lagCompLimit)).forEach { tick ->
                    state.advance(tick - 1)
                    simulationTickProvider.tick++
                    globalTickProvider.tick++
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

    override fun deltaAvailable(tick: Long): Boolean {
        return incomingDataBuffer.deltas.containsKey(tick).also { logger.debug { "Delta present for tick $tick: $it" } }
            && incomingDataBuffer.inputs2.containsKey(tick).also { logger.debug { "Input present for tick $tick: $it" } }
            && incomingDataBuffer.playersSawTicks.containsKey(tick).also { logger.debug { "sawTicks present for tick $tick: $it" } }
            && incomingDataBuffer.playersDeltas.containsKey(tick).also { logger.debug { "playersDelta present for tick $tick: $it" } }
    }
}
