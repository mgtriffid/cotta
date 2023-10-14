package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.server.CottaServer
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.cotta.server.guice.CottaServerFactory
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PannaServer {

    fun start() {
        val server = CottaServerFactory().create(PannaGame());
        server.initializeInstances()
        logger.info { "Server started" }
    }
}
