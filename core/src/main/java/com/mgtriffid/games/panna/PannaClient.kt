package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.panna.shared.PannaGame

class PannaClient {
    private val cottaClient = CottaClient(PannaGame(), KryonetCottaNetwork())

    fun initialize() = cottaClient.initialize()

    fun update() {
        cottaClient.update()
    }
}
