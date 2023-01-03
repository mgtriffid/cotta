package com.mgtriffid.games.cotta.network

/**
 * What are the requirements to this ServerNetwork? Should be updatable probably.
 */
interface CottaServerNetwork {
    fun connections(): Set<ClientConnection>
}
