package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.mgtriffid.games.cotta.network.ClientConnection
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class KryonetCottaServerNetwork : CottaServerNetwork {
    lateinit var server: Server
    private val enterGameIntents = ConcurrentLinkedQueue<Pair<ConnectionId, EnterGameIntent>>()

    override fun initialize() {
        logger.info { "Initializing ${KryonetCottaServerNetwork::class.simpleName}..." }
        server = Server()
        server.kryo.registerClasses()
        configureListener()
        server.bind(16001, 16002)
        server.start()
    }

    private fun configureListener() {
        val listener = ServerListener(
            enterGameIntents = enterGameIntents
        )
        server.addListener(listener)
    }

    override fun drainEnterGameIntents(): Collection<Pair<ConnectionId, EnterGameIntent>> {
        return enterGameIntents.drain()
    }

    override fun send(connectionId: ConnectionId, any: Any) {
        server.sendToUDP(connectionId.id, any)
    }

    /*override*/ fun dispatch(data: String) {
        // todo not to all but to specific
        server.sendToAllUDP(data)
    }

    override fun connections(): Set<ClientConnection> {
        TODO("Not yet implemented")
    }
}
