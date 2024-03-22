package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.protocol.ClientToServerCreatedPredictedEntitiesDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto2
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class ServerListener(
    private val enterGameIntents: ConcurrentLinkedQueue<Pair<ConnectionId, EnterGameIntent>>,
    private val clientToServerInputs: ConcurrentLinkedQueue<Pair<ConnectionId, ClientToServerInputDto>>,
    private val clientToServerInputs2: ConcurrentLinkedQueue<Pair<ConnectionId, ClientToServerInputDto2>>,
    private val clientToServerCreatedPredictedEntities: ConcurrentLinkedQueue<Pair<ConnectionId, ClientToServerCreatedPredictedEntitiesDto>>
) : Listener {
    override fun connected(connection: Connection?) {
        super.connected(connection)
    }

    override fun disconnected(connection: Connection?) {
        super.disconnected(connection)
        // Notify tracker of connections;
        // Offer to disconnect gracefully;
        // "intent to exit ghe game" to not let it be like in Diablo where one could just <Alt>+<F4> out of danger.
    }

    override fun received(connection: Connection, obj: Any?) {
        logger.debug { "Received an incoming $obj from ${connection.id}" }
        when (obj) {
            is EnterTheGameDto -> {
                enterGameIntents.add(Pair(ConnectionId(connection.id), deserialize(obj)))
            }
            is ClientToServerInputDto -> {
                logger.trace { "Received ${ClientToServerInputDto::class.simpleName}" }
                clientToServerInputs.add(Pair(ConnectionId(connection.id), obj))
            }
            is ClientToServerInputDto2 -> {
                logger.debug { "Received ${ClientToServerInputDto2::class.simpleName}" }
                clientToServerInputs2.add(Pair(ConnectionId(connection.id), obj))
            }
            is ClientToServerCreatedPredictedEntitiesDto -> {
                logger.trace { "Received ${ClientToServerCreatedPredictedEntitiesDto::class.simpleName}" }
                clientToServerCreatedPredictedEntities.add(Pair(ConnectionId(connection.id), obj))
            }
            else -> {
                logger.warn { "Received unknown object: $obj" }
            }
        }
    }

    private fun deserialize(obj: EnterTheGameDto): EnterGameIntent {
        return EnterGameIntent(HashMap(obj.params))
    }
}
