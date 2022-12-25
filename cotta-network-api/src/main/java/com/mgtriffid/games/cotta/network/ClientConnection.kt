package com.mgtriffid.games.cotta.network

/**
 * Represent connection object of a certain client on server.
 */
interface ClientConnection {
    val id: ConnectionId
}

data class ConnectionId(val id: Int)
