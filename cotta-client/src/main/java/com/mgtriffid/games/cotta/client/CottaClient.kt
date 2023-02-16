package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.CottaClientImpl
import com.mgtriffid.games.cotta.network.CottaClientNetwork

interface CottaClient {
    companion object {
        fun getInstance(network: CottaClientNetwork): CottaClient = CottaClientImpl(network)
    }

    fun tick()
}
