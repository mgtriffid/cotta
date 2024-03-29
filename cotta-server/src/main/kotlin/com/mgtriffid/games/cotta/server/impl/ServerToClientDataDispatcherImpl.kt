package com.mgtriffid.games.cotta.server.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.protocol.DeltaDto
import com.mgtriffid.games.cotta.network.protocol.FullStateDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto2
import com.mgtriffid.games.cotta.network.protocol.StateServerToClientDto2
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerToClientDataDispatcher
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerToClientDataDispatcherImpl<
    SR: StateRecipe,
    DR: DeltaRecipe,
    IR: InputRecipe,
    CEWTR: CreatedEntitiesWithTracesRecipe,
    MEDR: MetaEntitiesDeltaRecipe
    > @Inject constructor(
    @Named(SIMULATION) private val tick: TickProvider,
    private val clientsGhosts: ClientsGhosts<IR>,
    private val network: CottaServerNetworkTransport,
    // TODO these three better be merged into one somehow, verbose and redundant
    private val stateSnapper: StateSnapper<SR, DR, CEWTR, MEDR>,
    private val snapsSerialization: SnapsSerialization<SR, DR, CEWTR, MEDR>,
    private val inputSerialization: InputSerialization<IR>,
    private val data: DataForClients,
) : ServerToClientDataDispatcher {

    override fun dispatch() {
        val currentTick = tick.tick
        logger.debug { "Dispatching data to clients, currentTick=$currentTick" }
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val whatToSend = ghost.whatToSend(currentTick)
            logger.debug { "Sending data to $playerId : ${whatToSend.necessaryData}" }
            whatToSend.necessaryData.forEach { (tick, kind) ->
                logger.debug { "Sending tick $tick, kind $kind to $playerId" }
                network.sendAll(ghost.connectionId, packData(tick, kind, playerId))
                logger.debug { "Sent tick $tick, kind $kind to $playerId" }
            }
        }
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val whatToSend2 = ghost.whatToSend2()
            logger.debug { "Sending data to $playerId : ${whatToSend2}" }
//            network.send(ghost.connectionId, packData(currentTick, whatToSend2, playerId))
            logger.debug { "Sent data to $playerId : $whatToSend2" }
        }
    }

    private fun packData(tick: Long, whatToSend: WhatToSend2, playerId: PlayerId): ServerToClientDto2 {
        when (whatToSend) {
            WhatToSend2.STATE -> {
                val fullStateTick = tick - MAX_LAG_COMP_DEPTH_TICKS
                val dto = StateServerToClientDto2()
                dto.tick = tick
                dto.fullState = FullStateDto().apply {
                    payload = snapsSerialization.serializeStateRecipe(
                        stateSnapper.snapState(data.entities(fullStateTick))
                    )
                }
                dto.playerId = playerId.id
                dto.deltas = ((tick - MAX_LAG_COMP_DEPTH_TICKS + 1)..tick).map { t ->
                   DeltaDto().apply {
                       payload = snapsSerialization.serializeDeltaRecipe(
                           stateSnapper.snapDelta(
                               prev = data.entities(tick - 1),
                               curr = data.entities(tick)
                           )
                       )
                   }
                }
                return dto
            }
            WhatToSend2.SIMULATION_INPUTS -> {
                /*val inputDto2 = ServerToClientDto2()
                inputDto2.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.INPUT2
                inputDto2.payload = inputSerialization.serializePlayersInputs(
                    data.playerInputs()
                )
                inputDto2.tick = tick
                return inputDto2*/
                TODO()
            }
        }
    }

    private fun packData(tick: Long, kindOfData: KindOfData, playerId: PlayerId): Collection<ServerToClientDto> {
        when (kindOfData) {
            KindOfData.DELTA -> {
                val deltaDto = ServerToClientDto()
                deltaDto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.DELTA
                deltaDto.tick = tick - 1
                logger.debug { "Snapping delta for tick ${tick - 1} to $tick" }
                deltaDto.payload = snapsSerialization.serializeDeltaRecipe(
                    stateSnapper.snapDelta(
                        prev = data.entities(tick - 1),
                        curr = data.entities(tick)
                    )
                )

                val playersDeltaDto = ServerToClientDto()
                playersDeltaDto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.PLAYERS_DELTA
                playersDeltaDto.tick = tick - 1
                playersDeltaDto.payload = snapsSerialization.serializePlayersDeltaRecipe(
                    stateSnapper.snapPlayersDelta(data.players().addedAtTick(tick))
                )

                val inputDto2 = ServerToClientDto()
                inputDto2.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.INPUT2
                inputDto2.payload = inputSerialization.serializePlayersInputs(
                    data.playerInputs()
                )
                inputDto2.tick = tick - 1

                val playersSawTicksDto = ServerToClientDto()
                playersSawTicksDto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.PLAYERS_SAW_TICKS
                playersSawTicksDto.payload = snapsSerialization.serializePlayersSawTicks(
                    data.playersSawTicks().all().also { logger.debug { it.toString() } }
                )
                playersSawTicksDto.tick = tick - 1
                return listOf(
                    deltaDto,
                    inputDto2,
                    playersSawTicksDto,
                    playersDeltaDto,
                )
            }
            KindOfData.STATE -> {
                val dto = ServerToClientDto()
                dto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.STATE
                dto.payload = snapsSerialization.serializeStateRecipe(stateSnapper.snapState(data.entities(tick)))
                dto.tick = tick
                return listOf(dto)
            }
            KindOfData.PLAYER_ID -> {
                val dto = ServerToClientDto()
                dto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.PLAYER_ID
                dto.payload = snapsSerialization.serializePlayerId(
                    playerId
                )
                dto.tick = tick
                return listOf(dto)
            }
        }
    }
}

fun CottaServerNetworkTransport.sendAll(connectionId: ConnectionId, dtos: Collection<ServerToClientDto>) {
    dtos.forEach {
        logger.debug { "Sending ${it.kindOfData} with tick ${it.tick} to $connectionId" }
        send(connectionId, it)
    }
}
