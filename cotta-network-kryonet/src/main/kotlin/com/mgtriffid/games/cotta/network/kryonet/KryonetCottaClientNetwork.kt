package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class KryonetCottaClientNetwork: CottaClientNetwork {
    lateinit var client: Client

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

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(connection: Connection?, `object`: Any?) {
                logger.debug { "Received $`object`" }
            }
        }
        client.addListener(listener)
    }

}
