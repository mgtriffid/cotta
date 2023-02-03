package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.server.impl.ClientsGhosts
import com.mgtriffid.games.cotta.server.impl.ServerToClientDataChannelImpl

interface ServerToClientDataChannel {
    fun send(data: DataForClients)

    companion object {
        fun getInstance(
            tickProvider: TickProvider,
            clientsGhosts: ClientsGhosts
        ): ServerToClientDataChannel = ServerToClientDataChannelImpl(
            tick = tickProvider,
            clientsGhosts = clientsGhosts
        )
    }
}
