package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.CottaServer
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PannaServer {

    fun start() {
        val server = CottaServer(
            network = KryonetCottaNetwork(),
            game = PannaGame()
        )
        server.initializeInstances()
        logger.info { "Server started" }
    }
}
