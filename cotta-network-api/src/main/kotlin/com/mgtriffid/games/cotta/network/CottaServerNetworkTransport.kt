package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto2
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent

/**
 * What are the requirements to this ServerNetwork? Should be updatable probably.
 */
interface CottaServerNetworkTransport {
    fun connections(): Set<ClientConnection>

    fun initialize() // questionable

    fun drainEnterGameIntents(): Collection<Pair<ConnectionId, EnterGameIntent>>

    fun drainInputs2(): Collection<Pair<ConnectionId, ClientToServerInputDto2>>

    fun send(connectionId: ConnectionId, any: Any)
}
