package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.impl.CottaEngineImpl
import com.mgtriffid.games.cotta.network.CottaNetwork
import com.mgtriffid.games.cotta.server.impl.ClientsGhosts
import com.mgtriffid.games.cotta.server.impl.CottaGameInstanceImpl
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CottaServer(
    private val network: CottaNetwork,
    private val game: CottaGame
) {
    fun initializeInstances() {
        // TODO make multi-threaded
        val instance = createGameInstance()
        // start a thread that will just run
        val thread = Thread { instance.run() }
        thread.name = "game-main-loop-thread-0"
        thread.start()
    }

    private fun createGameInstance(): CottaGameInstance {
        logger.info { "Initializing game instance" }

        // TODO sus dependencies
        val serverNetwork = network.createServerNetwork()
        val engine = CottaEngineImpl()
        val clientsGhosts = ClientsGhosts()
        return CottaGameInstanceImpl(
            game,
            engine,
            serverNetwork,
            ClientsInputProvider.create(
                serverNetwork,
                engine.getInputSerialization(),
                engine.getInputSnapper(),
                clientsGhosts,
            ),
            clientsGhosts
        )
    }
}
