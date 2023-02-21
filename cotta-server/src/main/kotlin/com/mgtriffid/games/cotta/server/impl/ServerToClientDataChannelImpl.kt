package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.idiotic.ComponentDto
import com.mgtriffid.games.cotta.network.idiotic.EntityDeltaDto
import com.mgtriffid.games.cotta.network.idiotic.EntityDto
import com.mgtriffid.games.cotta.network.idiotic.IdioticSerializationDeserialization
import com.mgtriffid.games.cotta.network.idiotic.ServerToClientDeltaDto
import com.mgtriffid.games.cotta.network.idiotic.ServerToClientPacket
import com.mgtriffid.games.cotta.network.idiotic.ServerToClientStateDto
import com.mgtriffid.games.cotta.network.protocol.serialization.ServerToClientGameDataPiece
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerToClientDataChannel
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerToClientDataChannelImpl(
    private val tick: TickProvider,
    private val clientsGhosts: ClientsGhosts,
    private val network: CottaServerNetwork,
    private val serialization: IdioticSerializationDeserialization
) : ServerToClientDataChannel {

    // Here we don't care much about data that comes _from_ client and about their sawTick values.
    // We do, however, care about data that is being sent _to_ clients. We record acks, we record what entities
    // did clients see and what entities they did not see. We also care about predicted entities of clients. Yes,
    // when client predicts an entity spawn, we should let it know that "ok bro we have acknowledged your prediction,
    // you predicted it as id `predicted_1`, we gave it id `543`, now we match all input you give for that entity,
    // and when we send you this entity back, your job is to start treating `543` as the id, not `predicted_1`.

    override fun send(data: DataForClients) {
        val tick = tick.tick
        clientsGhosts.data.forEach {
            it.value.send(data, tick)
        }
        actuallySendData(clientsGhosts)
    }

    private fun actuallySendData(clientGhosts: ClientsGhosts) {
        clientGhosts.data.forEach { (_, ghost) ->
            val packets = ghost.drainQueue()
            logger.debug { "About to send data to connection ${ghost.connectionId.id}" }
            packets.forEach {
                network.send(ghost.connectionId, it.toStatePacket())
            }
        }
    }

    private fun ServerToClientGameDataPiece.toStatePacket(): ServerToClientPacket {
        val packet = ServerToClientPacket()
        packet.tick = tick
        packet.data = when (this) {
            is ServerToClientGameDataPiece.StatePiece -> {
                val ret = ServerToClientStateDto()
                ret.entities = this.stateSnapshot.entities.map { entity ->
                    val dto = EntityDto()
                    dto.entityId = entity.id
                    dto.components = entity.components().map { component ->
                        val c = ComponentDto()
                        c.name = component::class.simpleName
                        c.data = serialization.serialize(component)
                        c
                    }
                    dto
                }
                ret
            }

            is ServerToClientGameDataPiece.DeltaPiece -> {
                val ret = ServerToClientDeltaDto()
                ret.addedEntities = ArrayList(this.delta.addedEntities.map { entity ->
                    val dto = EntityDto()
                    dto.entityId = entity.id
                    dto.components = entity.components().map { component ->
                        val c = ComponentDto()
                        c.name = component::class.simpleName
                        c.data = serialization.serialize(component)
                        c
                    }
                    dto
                })
                ret.removedEntityIds = ArrayList(this.delta.removedEntitiesIds.toList())
                ret.modifiedEntities = ArrayList(this.delta.changedEntities.map { entity ->
                    val dto = EntityDeltaDto()
                    dto.entityId = entity.id
                    dto.components = entity.components().map { component ->
                        val c = ComponentDto()
                        c.name = component::class.simpleName
                        c.data = serialization.serialize(component)
                        c
                    }
                    dto
                })
                ret
            }
        }
        return packet
    }
}
