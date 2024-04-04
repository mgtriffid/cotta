package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent

/**
 * What are the requirements to this ServerNetwork? Should be updatable probably.
 */
interface CottaServerNetworkTransport {

    fun initialize() // questionable

    fun drainEnterGameIntents(): Collection<Pair<ConnectionId, EnterGameIntent>>

    fun drainInputs(): Collection<Pair<ConnectionId, ClientToServerInputDto>>

    fun send(connectionId: ConnectionId, obj: Any)
}
