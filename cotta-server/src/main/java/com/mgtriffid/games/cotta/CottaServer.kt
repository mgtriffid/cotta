package com.mgtriffid.games.cotta

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaNetwork
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CottaServer(
    private val network: CottaNetwork,
    private val game: CottaGame
) {
    private val instances: MutableMap<GameInstanceId, CottaGameInstance> = HashMap()

    fun initializeInstances() {
        // TODO make multithreaded
        val instance = createGameInstance()
        instances[GameInstanceId(0)] = instance
        // start a thread that will just run
        val thread = Thread { instance.run() }
        thread.name = "game-main-loop-thread-0"
        thread.start()
    }

    private fun createGameInstance(): CottaGameInstance {
        // Pass network
        // Pass actually everything
        return CottaGameInstanceImpl()
    }
}
