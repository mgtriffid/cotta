package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.server.impl.ClientsGhosts
import com.mgtriffid.games.cotta.server.impl.ServerToClientDataChannelImpl

interface ServerToClientDataChannel {
    fun send(data: DataForClients)

    companion object {
        fun getInstance(
            tickProvider: () -> Long,
            clientsGhosts: ClientsGhosts
        ): ServerToClientDataChannel = ServerToClientDataChannelImpl(
            tickProvider = tickProvider,
            clientsGhosts = clientsGhosts
        )
    }
}
