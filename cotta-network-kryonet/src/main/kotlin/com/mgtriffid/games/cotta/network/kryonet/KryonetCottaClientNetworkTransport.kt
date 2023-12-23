package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.protocol.ClientToServerCreatedPredictedEntitiesDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class KryonetCottaClientNetworkTransport: CottaClientNetworkTransport {
    private lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<ServerToClientDto>()

    // TODO would be more clear to separate init and connect.
    override fun initialize() {
        client = Client()
        client.kryo.registerClasses()
        configureListener()
        client.start()
        // TODO configuration
        client.connect(5000, "127.0.0.1", 16001, 16002)
    }

    override fun sendEnterGameIntent() {
        client.sendUDP(EnterTheGameDto())
    }

    override fun drainIncomingData(): Collection<ServerToClientDto> {
        return packetsQueue.drain()
    }

    override fun sendInput(input: ClientToServerInputDto) {
        client.sendUDP(input)
    }

    override fun sendCreatedEntities(createdEntitiesDto: ClientToServerCreatedPredictedEntitiesDto) {
        client.sendUDP(createdEntitiesDto)
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(connection: Connection?, obj: Any?) {
                logger.debug { "Received $obj" }
                when (obj) {
                    is ServerToClientDto -> {
                        packetsQueue.add(obj)
                        logger.debug { "Tick ${obj.tick}, kind ${obj.kindOfData}" }
                    }
                }
            }
        }
        client.addListener(listener)
    }
}
