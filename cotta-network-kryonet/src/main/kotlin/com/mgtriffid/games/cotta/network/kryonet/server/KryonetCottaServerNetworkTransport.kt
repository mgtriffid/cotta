package com.mgtriffid.games.cotta.network.kryonet.server

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.Server
import com.mgtriffid.games.cotta.network.ClientConnection
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.ServerListener
import com.mgtriffid.games.cotta.network.kryonet.registerClasses
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class KryonetCottaServerNetworkTransport : CottaServerNetworkTransport {
    private lateinit var server: Server
    private val enterGameIntents =
        ConcurrentLinkedQueue<Pair<ConnectionId, EnterGameIntent>>()
    private val clientToServerInputs =
        ConcurrentLinkedQueue<Pair<ConnectionId, ClientToServerInputDto>>()
    val kryo: Kryo
        get() = server.kryo

    override fun initialize() {
        logger.info { "Initializing ${KryonetCottaServerNetworkTransport::class.simpleName}..." }
        server = Server()
        server.kryo.registerClasses()
        configureListener()
        server.bind(16001, 16002)
        server.start()
    }

    private fun configureListener() {
        val listener = ServerListener(
            enterGameIntents = enterGameIntents,
            clientToServerInputs = clientToServerInputs,
        )
        server.addListener(listener)
    }

    override fun drainEnterGameIntents(): Collection<Pair<ConnectionId, EnterGameIntent>> {
        return enterGameIntents.drain()
    }

    override fun drainInputs(): Collection<Pair<ConnectionId, ClientToServerInputDto>> {
        return clientToServerInputs.drain()
    }

    override fun send(connectionId: ConnectionId, obj: Any) {
        server.sendToUDP(connectionId.id, obj)
    }
}
