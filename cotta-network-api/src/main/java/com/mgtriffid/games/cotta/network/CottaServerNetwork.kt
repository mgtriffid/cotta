package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent

/**
 * What are the requirements to this ServerNetwork? Should be updatable probably.
 */
interface CottaServerNetwork {
    fun connections(): Set<ClientConnection>

    fun initialize() // questionnable

    fun drainEnterGameIntents(): Collection<EnterGameIntent>
}
