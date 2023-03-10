package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.core.serialization.ServerToClientGameDataPiece

interface CottaClientNetwork {
    fun initialize()

    fun sendEnterGameIntent()

    fun drainIncomingData(): Collection<Any>
}
