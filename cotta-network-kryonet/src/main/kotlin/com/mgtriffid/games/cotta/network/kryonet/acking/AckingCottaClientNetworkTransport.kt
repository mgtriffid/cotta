package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.google.common.cache.CacheBuilder
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.Saver
import com.mgtriffid.games.cotta.network.kryonet.client.Sender
import com.mgtriffid.games.cotta.network.kryonet.registerClasses
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

typealias KryoConnection = com.esotericsoftware.kryonet.Connection

const val DATAGRAM_SIZE = 512

private val logger = KotlinLogging.logger {}

class AckingCottaClientNetworkTransport(
    private val sender: Sender,
    private val saver: Saver
) : CottaClientNetworkTransport {

    private val receivedPackets = ReceivedPackets()
    private var packetSequence = 0

    private val incomingSquadrons = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build<SquadronId, IncomingSquadron>()

    private lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<ServerToClientDto>()

    private val connection = Connection(
        serialize = { obj ->
            Output(1024 * 1024).also { output ->
                client.kryo.writeClassAndObject(output, obj)
            }.toBytes()
        },
        deserialize = { bytes ->
            val input = Input(bytes)
            client.kryo.readClassAndObject(input)
        },
        sendChunk = { chunk -> client.sendUDP(chunk) },
        saveObject = { save(it) }
    )

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

    override fun send(obj: Any) {
        connection.send(obj)
        sender.send(client, obj)
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(kryoConnection: KryoConnection?, obj: Any?) {
                when (obj) {
                    is Chunk -> {
                        logger.info { "Received a ${Chunk::class.simpleName} of size ${obj.data.size}" }
                        saver.save(obj, connection::receiveChunk)
                    }
                }
            }
        }
        client.addListener(listener)
    }

    private fun save(obj: Any) {
        when (obj) {
            is ServerToClientDto -> {
                packetsQueue.add(obj)
            }

            else -> {
                logger.warn { "Unknown object received: $obj" }
            }
        }
    }
}

class IncomingSquadron(
    val data: Array<ByteArray?>
)

