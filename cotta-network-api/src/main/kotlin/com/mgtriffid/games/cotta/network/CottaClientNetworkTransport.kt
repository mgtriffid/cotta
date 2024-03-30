package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto2

interface CottaClientNetworkTransport {
    fun initialize()

    fun sendEnterGameIntent()

    fun drainIncomingData(): Collection<ServerToClientDto>

    fun drainIncomingData2(): Collection<ServerToClientDto2>

    fun send(obj: Any)
}
