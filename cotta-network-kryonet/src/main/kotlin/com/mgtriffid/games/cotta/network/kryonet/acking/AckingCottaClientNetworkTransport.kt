package com.mgtriffid.games.cotta.network.kryonet.acking

import com.codahale.metrics.Histogram
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.SlidingTimeWindowArrayReservoir
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Listener
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
    private val sender: Sender, // TODO make use of
    private val saver: Saver,
    private val metricRegistry: MetricRegistry
) : CottaClientNetworkTransport {

    private lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<ServerToClientDto>()

    private lateinit var connection: Connection

    override fun initialize() {
        client = Client()
        client.kryo.registerClasses()
        configureListener()
        client.start()
        // TODO configuration
        client.connect(5000, "127.0.0.1", 16001, 16002)
        val serializer = chunkSerializer()
        connection = Connection(
            serializer = serializer,
            sendChunk = { chunk -> client.sendUDP(chunk) },
            saveObject = { save(it) }
        )
    }

    private fun chunkSerializer(): ChunkSerializer {
        val histogram = Histogram(SlidingTimeWindowArrayReservoir(3000, TimeUnit.MILLISECONDS))
        metricRegistry.register("sent_chunk_size", histogram)
        return MeasuringChunkSerializer(
            KryoChunkSerializer(client.kryo),
            histogram
        )
    }

    override fun sendEnterGameIntent() {
        send(EnterTheGameDto())
    }

    override fun drainIncomingData(): Collection<ServerToClientDto> {
        return packetsQueue.drain()
    }

    override fun send(obj: Any) {
        sender.send(obj, connection::send)
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(kryoConnection: KryoConnection?, obj: Any?) {
                when (obj) {
                    is Chunk -> {
                        logger.debug { "Received a ${Chunk::class.simpleName} of size ${obj.data.size}" }
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

