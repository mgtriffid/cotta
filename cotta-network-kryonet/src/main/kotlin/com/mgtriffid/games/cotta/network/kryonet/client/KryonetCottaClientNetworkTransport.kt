package com.mgtriffid.games.cotta.network.kryonet.client

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.registerClasses
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto2
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.UIDefaults.ActiveValue

private val logger = KotlinLogging.logger {}

internal class KryonetCottaClientNetworkTransport(
    private val sender: Sender,
    private val saver: Saver
) : CottaClientNetworkTransport {
    private lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<ServerToClientDto>()
    private val packetsQueue2 = ConcurrentLinkedQueue<ServerToClientDto2>()

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
        send(EnterTheGameDto())
    }

    override fun drainIncomingData(): Collection<ServerToClientDto> {
        return packetsQueue.drain()
    }

    override fun drainIncomingData2(): Collection<ServerToClientDto2> {
        return packetsQueue2.drain()
    }

    override fun send(obj: Any) {
        sender.send(client, obj)
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(connection: Connection?, obj: Any?) {
                logger.debug { "Received $obj" }
                when (obj) {
                    is ServerToClientDto -> {
                        saver.save(obj, packetsQueue)
                        logger.debug { "Tick ${obj.tick}, kind ${obj.kindOfData}" }
                    }
                    is ServerToClientDto2 -> {
                        saver.save(obj, packetsQueue2)
                        logger.info { "Received a ${ServerToClientDto2::class.simpleName}: $obj" }
                    }
                }
            }
        }
        client.addListener(listener)
    }

    private fun save(obj: ServerToClientDto) {
        packetsQueue.add(obj)
    }
}
