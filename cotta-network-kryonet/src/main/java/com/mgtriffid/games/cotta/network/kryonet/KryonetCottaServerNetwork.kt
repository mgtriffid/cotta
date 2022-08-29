package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Server
import com.mgtriffid.games.cotta.network.CottaServerNetwork

class KryonetCottaServerNetwork : CottaServerNetwork {
    lateinit var server: Server

    override fun initialize() {
        server = Server()
        configureListener()
        server.bind(16001, 16002)
        server.start()
    }

    private fun configureListener() {

    }
}
