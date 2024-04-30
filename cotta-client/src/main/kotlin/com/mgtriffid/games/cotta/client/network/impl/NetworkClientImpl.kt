package com.mgtriffid.games.cotta.client.network.impl

import com.codahale.metrics.MetricRegistry
import com.mgtriffid.games.cotta.client.ClientSimulationInput
import com.mgtriffid.games.cotta.client.impl.AuthoritativeState
import com.mgtriffid.games.cotta.client.impl.AuthoritativeStateData
import com.mgtriffid.games.cotta.client.impl.ClientIncomingDataBuffer
import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.client.impl.SimulationInputData
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.MAX_LAG_COMP_DEPTH_TICKS
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.ClientInputId
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
import com.mgtriffid.games.cotta.network.protocol.SimulationInputServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.StateServerToClientDto
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
    private val incomingDataBuffer: ClientIncomingDataBuffer<SR, DR>,
    private val snapsSerialization: SnapsSerialization<SR, DR, PDR>,
    private val inputSerialization: InputSerialization<IR>,
    private val stateSnapper: StateSnapper<SR, DR, PDR>,
    private val localPlayer: LocalPlayer,
    private val metrics: MetricRegistry
) : NetworkClient {
    private val bufferLength: Int = 3

    override fun initialize() {
        networkTransport.initialize()
    }

    override fun enterGame() {
        networkTransport.sendEnterGameIntent()
    }

    override fun disconnect() {
        networkTransport.disconnect()
    }

    override fun fetch() {
        networkTransport.drainIncomingData().forEach { packet ->
            when (packet) {
                is StateServerToClientDto -> {
                    incomingDataBuffer.storeState(
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

                is SimulationInputServerToClientDto -> {
                    incomingDataBuffer.storeSimulationInput(
                        packet.tick,
                        SimulationInputData(
                            tick = packet.tick,
                            playersSawTicks = snapsSerialization.deserializePlayersSawTicks(packet.playersSawTicks),
                            playersInputs = inputSerialization.deserializePlayersInputs(packet.playersInputs),
                            playersDiff = PlayersDiff(
                                added = packet.playersDiff.added.map { PlayerId(it) }.toSet()
                            ),
                            idSequence = packet.idSequence,
                            confirmedClientInput = ClientInputId(packet.confirmedClientInput)
                        )
                    )
                    recordBufferLength(packet)
                }
            }
        }
    }

    private fun recordBufferLength(packet: SimulationInputServerToClientDto) {
        logger.info { "Buffer length: ${packet.bufferLength}" }
        metrics.histogram("server_buffer_ahead")
            .update(packet.bufferLength.toInt())
    }

    override fun send(
        inputId: ClientInputId,
        input: PlayerInput,
        sawTick: Long
    ) {
        val inputDto = ClientToServerInputDto()
        inputDto.inputId = inputId.id
        inputDto.sawTick = sawTick
        logger.debug { "Sending input for $sawTick : $input" }
        inputDto.payload = inputSerialization.serializeInput(input)
        networkTransport.send(inputDto)
    }

    override fun tryGetDelta(tick: Long): Delta? {
        val data: SimulationInputData? = incomingDataBuffer.simulationInputs[tick]
        if (data == null) return null
        checkTick(data, tick)
        val input = ClientSimulationInput(
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
            idSequence = data.idSequence,
            confirmedClientInput = data.confirmedClientInput
        )
        return Delta(input)
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
                val tick = incomingDataBuffer.states.lastKey()
                val stateData = incomingDataBuffer.states[tick]!!
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

    private fun stateAvailable(): Boolean = incomingDataBuffer.states.isNotEmpty()

    private fun bufferedEnough(): Boolean = incomingDataBuffer.simulationInputs.keys.containsAll(
        ((incomingDataBuffer.states.lastKey() + 1)..(incomingDataBuffer.states.lastKey() + bufferLength)).toList()
    )

    override fun deltaAvailable(tick: Long): Boolean {
        return incomingDataBuffer.simulationInputs.containsKey(tick)
    }
}
