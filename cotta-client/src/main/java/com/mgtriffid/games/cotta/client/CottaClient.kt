package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.CottaClientImpl

interface CottaClient {
    companion object {
        fun getInstance(): CottaClient = CottaClientImpl()
    }

    fun tick()
}
