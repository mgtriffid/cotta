package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.CottaClientImpl
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaClientNetwork

interface CottaClient {
    companion object {
        fun getInstance(
            game: CottaGame,
            network: CottaClientNetwork
        ): CottaClient = CottaClientImpl(game, network)
    }

    fun tick()
}
