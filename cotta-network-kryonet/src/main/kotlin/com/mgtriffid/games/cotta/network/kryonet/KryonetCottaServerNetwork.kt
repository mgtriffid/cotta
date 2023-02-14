package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.mgtriffid.games.cotta.network.ClientConnection
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class KryonetCottaServerNetwork : CottaServerNetwork {
    lateinit var server: Server
    private val enterGameIntents = ConcurrentLinkedQueue<EnterGameIntent>()

    override fun initialize() {
        logger.info { "Initializing ${KryonetCottaServerNetwork::class.simpleName}..." }
        server = Server()
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

    override fun drainEnterGameIntents(): Collection<EnterGameIntent> {
        return enterGameIntents.drain()
    }

    /*override*/ fun dispatch(data: String) {
        // todo not to all but to specific
        server.sendToAllUDP(data)
        server.connections
    }

    override fun connections(): Set<ClientConnection> {
        TODO("Not yet implemented")
    }
}

private class Listener : Listener {
    override fun connected(connection: Connection?) {
        super.connected(connection)
    }

    override fun disconnected(connection: Connection?) {
        super.disconnected(connection)
    }

    override fun received(connection: Connection?, `object`: Any?) {
        super.received(connection, `object`)
    }

    override fun idle(connection: Connection?) {
        super.idle(connection)
    }
}
