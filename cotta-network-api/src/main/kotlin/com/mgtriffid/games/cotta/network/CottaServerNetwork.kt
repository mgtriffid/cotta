package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ClientToServerCreatedPredictedEntitiesDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent

/**
 * What are the requirements to this ServerNetwork? Should be updatable probably.
 */
interface CottaServerNetwork {
    fun connections(): Set<ClientConnection>

    fun initialize() // questionable

    fun drainEnterGameIntents(): Collection<Pair<ConnectionId, EnterGameIntent>>

    // TODO pass and utilize tick
    fun drainInputs(): Collection<Pair<ConnectionId, ClientToServerInputDto>>

    fun drainCreatedEntities(): Collection<Pair<ConnectionId, ClientToServerCreatedPredictedEntitiesDto>>

    fun send(connectionId: ConnectionId, any: Any)
}
