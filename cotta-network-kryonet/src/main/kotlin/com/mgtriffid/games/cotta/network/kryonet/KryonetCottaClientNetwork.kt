package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class KryonetCottaClientNetwork: CottaClientNetwork {
    lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<Any>() // TODO

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

    override fun drainIncomingData(): Collection<Any> {
        return packetsQueue.drain()
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(connection: Connection?, obj: Any?) {
                logger.debug { "Received $obj" }
            }
        }
        client.addListener(listener)
    }
}
