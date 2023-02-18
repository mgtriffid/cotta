package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.serialization.ServerToClientGameDataPiece

interface CottaClientNetwork {
    fun initialize()

    fun sendEnterGameIntent()

    fun drainIncomingData(): Collection<ServerToClientGameDataPiece>
}
