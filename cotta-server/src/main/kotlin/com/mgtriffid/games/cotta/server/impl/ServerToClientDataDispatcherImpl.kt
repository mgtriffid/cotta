package com.mgtriffid.games.cotta.server.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.*
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.CreatedEntitiesWithTracesRecipe
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerToClientDataDispatcher
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerToClientDataDispatcherImpl<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> @Inject constructor(
    private val tick: TickProvider,
    private val clientsGhosts: ClientsGhosts,
    private val network: CottaServerNetwork,
    private val stateSnapper: StateSnapper<SR, DR>,
    private val snapsSerialization: SnapsSerialization<SR, DR>,
    private val inputSnapper: InputSnapper<IR>,
    private val inputSerialization: InputSerialization<IR>,
    private val data: DataForClients,
) : ServerToClientDataDispatcher {

    // Here we don't care much about data that comes _from_ client and about their sawTick values.
    // We do, however, care about data that is being sent _to_ clients. We record acks, we record what entities
    // did clients see and what entities they did not see. We also care about predicted entities of clients. Yes,
    // when client predicts an entity spawn, we should let it know that "ok bro we have acknowledged your prediction,
    // you predicted it as id `predicted_1`, we gave it id `543`, now we match all input you give for that entity,
    // and when we send you this entity back, your job is to start treating `543` as the id, not `predicted_1`".

    override fun dispatch() {
        val currentTick = tick.tick
        logger.debug { "Dispatching data to clients, currentTick=$currentTick" }
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val whatToSend = ghost.whatToSend(currentTick)
            whatToSend.necessaryData.forEach { (tick, kind) ->
                logger.debug { "Sending tick $tick, kind $kind to $playerId" }
                network.sendAll(ghost.connectionId, packData(tick, kind, data, playerId))
                logger.debug { "Sent tick $tick, kind $kind to $playerId" }
            }
        }
    }

    private fun packData(tick: Long, kindOfData: KindOfData, data: DataForClients, playerId: PlayerId): Collection<ServerToClientDto> {
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
                val inputDto = ServerToClientDto()
                inputDto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.INPUT
                inputDto.payload = inputSerialization.serializeInputRecipe(
                    inputSnapper.snapInput(data.inputs(tick - 1))
                )
                inputDto.tick = tick - 1

                val createdEntitiesWithTracesDto = ServerToClientDto()
                createdEntitiesWithTracesDto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.CREATED_ENTITIES_V2
                createdEntitiesWithTracesDto.payload = snapsSerialization.serializeEntityCreationTracesV2(CreatedEntitiesWithTracesRecipe(
                    data.createdEntities(tick).map { (trace, id) ->
                        Pair(stateSnapper.snapTrace(trace), id)
                    },
                    data.confirmedEntities(tick).associate { (predictedId, authoritativeId) ->
                        authoritativeId to predictedId
                    }
                ))
                createdEntitiesWithTracesDto.tick = tick

                val playersSawTicksDto = ServerToClientDto()
                playersSawTicksDto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.PLAYERS_SAW_TICKS
                playersSawTicksDto.payload = snapsSerialization.serializePlayersSawTicks(
                    data.playersSawTicks().all().also { logger.debug { it.toString() } }
                )
                playersSawTicksDto.tick = tick - 1
                return listOf(deltaDto, inputDto, createdEntitiesWithTracesDto, playersSawTicksDto)
            }
            KindOfData.STATE -> {
                val dto = ServerToClientDto()
                dto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.STATE
                dto.payload = snapsSerialization.serializeStateRecipe(stateSnapper.snapState(data.entities(tick)))
                dto.tick = tick
                return listOf(dto)
            }
            KindOfData.CLIENT_META_ENTITY_ID -> {
                val dto = ServerToClientDto()
                dto.kindOfData = com.mgtriffid.games.cotta.network.protocol.KindOfData.CLIENT_META_ENTITY_ID
                dto.payload = snapsSerialization.serializeMetaEntityId(
                    data.metaEntities()[playerId],
                    playerId
                )
                dto.tick = tick
                return listOf(dto)
            }
        }
    }
}

fun CottaServerNetwork.sendAll(connectionId: ConnectionId, dtos: Collection<ServerToClientDto>) {
    dtos.forEach {
        logger.debug { "Sending ${it.kindOfData} with tick ${it.tick} to $connectionId" }
        send(connectionId, it)
    }
}