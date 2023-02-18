package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.idiotic.EntityDto
import com.mgtriffid.games.cotta.network.idiotic.ServerToClientDeltaDto
import com.mgtriffid.games.cotta.network.idiotic.ServerToClientDto
import com.mgtriffid.games.cotta.network.idiotic.ServerToClientPacket
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.protocol.serialization.Delta
import com.mgtriffid.games.cotta.network.protocol.serialization.ServerToClientGameDataPiece
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class KryonetCottaClientNetwork: CottaClientNetwork {
    lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<ServerToClientPacket>()

    override fun initialize() {
        client = Client()
        client.kryo.registerClasses()
        configureListener()
        client.start()
        client.connect(5000, "127.0.0.1", 16001, 16002)
    }

    override fun sendEnterGameIntent() {
        client.sendUDP(EnterTheGameDto())
    }

    override fun drainIncomingData(): Collection<ServerToClientGameDataPiece> {
        return packetsQueue.drain().map(::deserialize)
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(connection: Connection?, obj: Any?) {
                logger.debug { "Received $obj" }
                when (obj) {
                    is ServerToClientPacket -> packetsQueue.add(obj)
                }
            }
        }
        client.addListener(listener)
    }

    private fun deserialize(packet: ServerToClientPacket): ServerToClientGameDataPiece = packet.data.let {
        when (it) {
            is ServerToClientDeltaDto -> {
                ServerToClientGameDataPiece.DeltaPiece(
                    tick = packet.tick,
                    delta = Delta(
                        removedEntitiesIds = it.removedEntityIds.toSet(),
                        addedEntities = it.addedEntities.map { entityDto ->
                            entityDto.toEntity()
                        },
                        changedEntities = it.modifiedEntities.toEntities(),
                        tick = packet.tick
                    )
                )
            }
            else -> TODO()
        }
    }
}

private fun EntityDto.toEntity(): Entity {
    TODO("Not yet implemented")
}

/* TODO no need to actually convert to Entity, because we don't need conversion to Entity. We need a recipe saying how
 to build an entity */
private fun <E> MutableList<E>.toEntities(): List<Entity> {
    TODO("Not yet implemented")
}
