package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.registerClasses
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.utils.drain
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

// TODO more flexible configuration: allow larger datagrams through config,
//  allow more chunks than 256, etc.
class AckingCottaServerNetworkTransport : CottaServerNetworkTransport {
    private lateinit var server: Server
    private val enterGameIntents =
        ConcurrentLinkedQueue<Pair<ConnectionId, EnterGameIntent>>()
    private val clientToServerInputs =
        ConcurrentLinkedQueue<Pair<ConnectionId, ClientToServerInputDto>>()
    private val chunkSerializer = KryoChunkSerializer()

    private val connections = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build<ConnectionId, Connection>(CacheLoader.from { connectionId ->
            Connection(
                serializer = chunkSerializer,
                sendChunk = { chunk ->
                    server.sendToUDP(
                        connectionId.id,
                        chunk
                    )
                },
                saveObject = { save(it, connectionId) }
            )
        })

    override fun initialize() {
        logger.info { "Initializing ${this::class.simpleName}..." }
        server = Server()
        listOf(server.kryo, chunkSerializer.kryoSer, chunkSerializer.kryoDeser).forEach { it.registerClasses() }
        configureListener()
        server.bind(16001, 16002)
        server.start()
    }

    private fun configureListener() {
        val listener = object : Listener {
            override fun received(kryoCconnection: KryoConnection, obj: Any?) {
                when (obj) {
                    is Chunk -> {
                        val connectionId = ConnectionId(kryoCconnection.id)
                        connections[connectionId].receiveChunk(obj)
                    }

                    else -> {
                        logger.warn { "Received unknown object: $obj" }
                    }
                }
            }
        }

        server.addListener(listener)
    }

    private fun save(obj: Any, connectionId: ConnectionId) {
        when (obj) {
            is EnterTheGameDto -> {
                enterGameIntents.add(
                    Pair(
                        connectionId,
                        EnterGameIntent(HashMap(obj.params))
                    )
                )
            }

            is ClientToServerInputDto -> {
                logger.debug { "Received ${ClientToServerInputDto::class.simpleName}" }
                clientToServerInputs.add(Pair(connectionId, obj))
            }

            else -> {
                logger.warn { "Received unknown object: $obj" }
            }
        }
    }

    override fun drainEnterGameIntents(): Collection<Pair<ConnectionId, EnterGameIntent>> {
        return enterGameIntents.drain()
    }

    override fun drainInputs(): Collection<Pair<ConnectionId, ClientToServerInputDto>> {
        return clientToServerInputs.drain()
    }

    override fun send(connectionId: ConnectionId, obj: Any) {
        connections[connectionId].send(obj)
    }
}
