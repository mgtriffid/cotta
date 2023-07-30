package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.impl.CottaEngineImpl
import com.mgtriffid.games.cotta.network.CottaNetwork
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.impl.CottaGameInstanceImpl
import mu.KotlinLogging
import kotlin.math.log

private val logger = KotlinLogging.logger {}

class CottaServer(
    private val network: CottaNetwork,
    private val game: CottaGame
) {
    private val instances: MutableMap<GameInstanceId, CottaGameInstance> = HashMap()

    fun initializeInstances() {
        // TODO make multi-threaded
        val instance = createGameInstance()
        instances[GameInstanceId(0)] = instance
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
        return CottaGameInstanceImpl(
            game,
            engine,
            serverNetwork,
            ClientsInput.create(
                serverNetwork,
                engine.getInputSerialization(),
                engine.getInputSnapper()
            )
        )
    }
}
