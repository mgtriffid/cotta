package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto
import com.mgtriffid.games.cotta.network.protocol.RegularDuringGameDto
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class ServerListener(
    private val enterGameIntents: ConcurrentLinkedQueue<Pair<ConnectionId, EnterGameIntent>>
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
        when (obj) {
            is EnterTheGameDto -> {
                enterGameIntents.add(Pair(ConnectionId(connection.id), deserialize(obj)))
            }
            is RegularDuringGameDto -> {
                // here goes some brutal deserialization
            }
        }
    }

    private fun deserialize(obj: EnterTheGameDto): EnterGameIntent {
        return EnterGameIntent(HashMap(obj.params))
    }
}
