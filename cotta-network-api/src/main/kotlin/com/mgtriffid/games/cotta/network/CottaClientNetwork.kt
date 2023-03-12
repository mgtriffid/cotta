package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto

interface CottaClientNetwork {
    fun initialize()

    fun sendEnterGameIntent()

    fun drainIncomingData(): Collection<ServerToClientDto>
}
