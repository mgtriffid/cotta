package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto

interface CottaClientNetworkTransport {
    fun initialize()

    fun sendEnterGameIntent()

    fun drainIncomingData(): Collection<ServerToClientDto>

    fun send(obj: Any)

    fun disconnect()
}
