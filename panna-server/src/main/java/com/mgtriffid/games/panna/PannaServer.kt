package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.CottaServer
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.panna.shared.PannaGame

class PannaServer {
    fun start() {
        val game = PannaGame()
        CottaServer(game, KryonetCottaNetwork()).start()
    }
}
