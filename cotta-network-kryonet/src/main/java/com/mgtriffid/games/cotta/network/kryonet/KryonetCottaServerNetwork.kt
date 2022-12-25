package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.mgtriffid.games.cotta.network.ClientConnection
import com.mgtriffid.games.cotta.network.CottaServerNetwork

class KryonetCottaServerNetwork : CottaServerNetwork {
    lateinit var server: Server

    fun initialize() {
        server = Server()
        configureListener()
        server.bind(16001, 16002)
        server.start()
    }

    private fun configureListener() {

    }

    /*override*/ fun dispatch(data: String) {
        // todo not to all but to specific
        server.sendToAllUDP(data)
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
