package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.Saver
import com.mgtriffid.games.cotta.network.kryonet.client.Sender
import com.mgtriffid.games.cotta.network.kryonet.registerClasses
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.SimulationInputServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.StateServerToClientDto
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.ArrayList
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

const val DATAGRAM_SIZE = 24

private val logger = KotlinLogging.logger {}

class AckingCottaClientNetworkTransport(
    private val sender: Sender,
    private val saver: Saver
) : CottaClientNetworkTransport {

    private val incomingSquadrons = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build<SquadronId, IncomingSquadron>()

    private lateinit var client: Client
    private val packetsQueue = ConcurrentLinkedQueue<ServerToClientDto>()

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
        sender.send(client, obj)
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(connection: Connection?, obj: Any?) {
                logger.debug { "Received $obj" }
                when (obj) {
                    is Chunk -> {
                        logger.info { "Received a ${Chunk::class.simpleName} of size ${obj.data.size}" }
                        saver.save(obj, ::save)
                    }
                }
            }
        }
        client.addListener(listener)
    }

    private fun save(chunk: Chunk) {
        val squadron = incomingSquadrons.get(SquadronId(chunk.squadron)) {
            IncomingSquadron(Array(chunk.size.toInt()) { null })
        }
        logger.info { "Incoming chunk for squadron ${chunk.squadron}, part ${chunk.packet}" }
        squadron.data[chunk.packet.toInt()] = chunk.data
        if (squadron.data.all { it != null }) {
            logger.info { "Squadron ${chunk.squadron} is complete" }
            val bytes = squadron.data.fold(ByteArray(0)) { acc, bytes ->
                acc + bytes!!
            }
            val input = Input(bytes)
            val dto = client.kryo.readClassAndObject(input) as ServerToClientDto
            logger.info { "DTO: ${dto::class.java.simpleName}, ${
                when (dto) {
                    is StateServerToClientDto -> dto.tick
                    is SimulationInputServerToClientDto -> dto.tick
                    else -> "unexpected"
                }
            }" }
            packetsQueue.add(dto)
        }
    }
}

class IncomingSquadron(
    val data: Array<ByteArray?>
)
