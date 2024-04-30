package com.mgtriffid.games.cotta.server.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.MAX_LAG_COMP_DEPTH_TICKS
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.protocol.DeltaDto
import com.mgtriffid.games.cotta.network.protocol.FullStateDto
import com.mgtriffid.games.cotta.network.protocol.PlayersDiffDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.SimulationInputServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.StateServerToClientDto
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerSimulationInputProvider
import com.mgtriffid.games.cotta.server.ServerToClientDataDispatcher
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerToClientDataDispatcherImpl<
    SR : StateRecipe,
    DR : DeltaRecipe,
    IR : InputRecipe,
    PDR : PlayersDeltaRecipe
    > @Inject constructor(
    @Named(SIMULATION) private val tick: TickProvider,
    private val clientsGhosts: ClientsGhosts<IR>,
    private val network: CottaServerNetworkTransport,
    // TODO these three better be merged into one somehow, verbose and redundant
    private val stateSnapper: StateSnapper<SR, DR, PDR>,
    private val snapsSerialization: SnapsSerialization<SR, DR, PDR>,
    private val inputSerialization: InputSerialization<IR>,
    private val data: DataForClients,
    private val inputProvider: ServerSimulationInputProvider,
) : ServerToClientDataDispatcher {

    override fun dispatch() {
        val currentTick = tick.tick
        logger.debug { "Dispatching data to clients, currentTick=$currentTick" }
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val whatToSend = ghost.whatToSend()
            logger.debug { "Sending data to $playerId : $whatToSend" }
            network.send(
                ghost.connectionId,
                packData(currentTick, whatToSend, playerId)
            )
        }
    }

    private fun packData(
        tick: Long,
        whatToSend: WhatToSend,
        playerId: PlayerId
    ): ServerToClientDto {
        when (whatToSend) {
            WhatToSend.STATE -> {
                val fullStateTick = tick - MAX_LAG_COMP_DEPTH_TICKS
                val dto = StateServerToClientDto()
                dto.tick = tick
                dto.fullState = FullStateDto().apply {
                    payload = snapsSerialization.serializeStateRecipe(
                        stateSnapper.snapState(data.entities(fullStateTick))
                    )
                }
                dto.playerId = playerId.id
                dto.deltas =
                    ((tick - MAX_LAG_COMP_DEPTH_TICKS + 1)..tick).map { t ->
                        DeltaDto().apply {
                            payload = snapsSerialization.serializeDeltaRecipe(
                                stateSnapper.snapDelta(
                                    prev = data.entities(tick - 1),
                                    curr = data.entities(tick)
                                )
                            )
                        }
                    }
                dto.playerIds = data.players().all().map { it.id }.toIntArray()
                return dto
            }

            WhatToSend.SIMULATION_INPUTS -> {
                val dto =
                    SimulationInputServerToClientDto()
                dto.tick = tick - 1
                dto.playersSawTicks =
                    snapsSerialization.serializePlayersSawTicks(
                        data.playersSawTicks().all()
                    )
                dto.playersInputs = inputSerialization.serializePlayersInputs(
                    data.playerInputs()
                )
                dto.playersDiff = PlayersDiffDto().apply {
                    added = data.players().addedAtTick(tick).map { it.id }
                        .toIntArray()
                    removed = data.players().removedAtTick(tick).map { it.id }
                        .toIntArray()
                }
                dto.idSequence = data.idSequence(tick)
                dto.confirmedClientInput =
                    clientsGhosts.data[playerId]?.lastUsedInput()?.id ?: 0
                dto.bufferLength = Math.min(
                    inputProvider.bufferAheadLength(
                        playerId
                    ), 127
                ).toByte()
                return dto
            }
        }
    }
}
