package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.panna.shared.PannaGame

class PannaClient {
    val cottaClient = CottaClient(PannaGame())

    fun update() {
        println("Panna client updated")
    }
}
