package com.mgtriffid.games.cotta.client.network.impl

import com.mgtriffid.games.cotta.client.ClientSimulationInput
import com.mgtriffid.games.cotta.client.impl.AuthoritativeState
import com.mgtriffid.games.cotta.client.impl.AuthoritativeStateData
import com.mgtriffid.games.cotta.client.impl.ClientIncomingDataBuffer
import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.client.impl.SimulationInputData
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.MAX_LAG_COMP_DEPTH_TICKS
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
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import com.mgtriffid.games.cotta.network.protocol.SimulationInputServerToClientDto2
import com.mgtriffid.games.cotta.network.protocol.StateServerToClientDto2
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

                KindOfData.INPUT -> incomingDataBuffer.storeInput(
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

    override fun fetch2() {
        networkTransport.drainIncomingData2().forEach { packet ->
            when (packet) {
                is StateServerToClientDto2 -> {
                    incomingDataBuffer.storeState2(
                        packet.tick,
                        AuthoritativeStateData(
                            packet.tick,
                            snapsSerialization.deserializeStateRecipe(packet.fullState.payload),
                            packet.deltas.map { snapsSerialization.deserializeDeltaRecipe(it.payload) },
                            PlayerId(packet.playerId),
                            packet.playerIds.map { PlayerId(it) }.toSet()
                        )
                    )
                }

                is SimulationInputServerToClientDto2 -> {
                    incomingDataBuffer.storeSimulationInput2(
                        packet.tick,
                        SimulationInputData(
                            tick = packet.tick,
                            playersSawTicks = snapsSerialization.deserializePlayersSawTicks(packet.playersSawTicks),
                            playersInputs = inputSerialization.deserializePlayersInputs(packet.playersInputs),
                            playersDiff = PlayersDiff(
                                added = packet.playersDiff.added.map { PlayerId(it) }.toSet()
                            ),
                            idSequence = packet.idSequence
                        )
                    )

                }
            }
        }
    }

    override fun send(input: PlayerInput, currentTick: Long) {
        val inputDto =
            ClientToServerInputDto()
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
                    return incomingDataBuffer.inputs[tick]!!.also { logger.debug { "In getting delta: $it" } }
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return incomingDataBuffer.playersSawTicks[tick]!!
                }

                override fun playersDiff() = PlayersDiff(
                    incomingDataBuffer.playersDeltas[tick]!!
                        .addedPlayers.toSet()
                )
            },
        )
    } else {
        null
    }

    fun getDelta2(tick: Long): Any? {
        val data: SimulationInputData? = incomingDataBuffer.simulationInputs[tick]
        if (data == null) return null
        checkTick(data, tick)
        ClientSimulationInput(
            tick = tick,
            simulationInput = object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return data.playersInputs
                }

                override fun nonPlayerInput(): NonPlayerInput {
                    return NonPlayerInput.Blank
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return data.playersSawTicks
                }

                override fun playersDiff() = data.playersDiff
            },
            idSequence = data.idSequence)
        TODO()
    }

    private fun checkTick(data: SimulationInputData, tick: Long) {
        if (data.tick != tick) {
            throw IllegalStateException("Tick mismatch: expected $tick, got ${data.tick}")
        }
    }

    override fun tryGetAuthoritativeState(): AuthoritativeState {
        return if (stateAvailable() && bufferedEnough()) {
            AuthoritativeState.Ready { state, simulationTickProvider, globalTickProvider ->
                logger.debug { "Setting state from authoritative" }
                val tick = incomingDataBuffer.states2.lastKey()
                val stateData = incomingDataBuffer.states2[tick]!!
                val fullStateTick = tick - MAX_LAG_COMP_DEPTH_TICKS
                state.setBlank(fullStateTick)
                simulationTickProvider.tick = fullStateTick
                globalTickProvider.tick = fullStateTick
                stateSnapper.unpackStateRecipe(state.entities(fullStateTick), stateData.state)
                // TODO review these indices CAREFULLY, it's easy to miss smth
                ((fullStateTick + 1)..tick).forEach { t ->
                    state.advance(t - 1)
                    simulationTickProvider.tick++
                    globalTickProvider.tick++
                    val delta =
                        stateData.deltas[(t - fullStateTick - 1).toInt()]
                    stateSnapper.unpackDeltaRecipe(
                        state.entities(atTick = t),
                        delta
                    )
                }
                localPlayer.set(stateData.playerId)
            }
        } else {
            AuthoritativeState.NotReady
        }
    }

    private fun stateAvailable(): Boolean = incomingDataBuffer.states2.isNotEmpty()

    private fun bufferedEnough(): Boolean = incomingDataBuffer.simulationInputs.keys.containsAll(
        ((incomingDataBuffer.states2.lastKey() + 1)..(incomingDataBuffer.states2.lastKey() + bufferLength)).toList()
    )

    override fun deltaAvailable(tick: Long): Boolean {
        return incomingDataBuffer.deltas.containsKey(tick).also { logger.debug { "Delta present for tick $tick: $it" } }
            && incomingDataBuffer.inputs.containsKey(tick).also { logger.debug { "Input present for tick $tick: $it" } }
            && incomingDataBuffer.playersSawTicks.containsKey(tick).also { logger.debug { "sawTicks present for tick $tick: $it" } }
            && incomingDataBuffer.playersDeltas.containsKey(tick).also { logger.debug { "playersDelta present for tick $tick: $it" } }
    }
}
