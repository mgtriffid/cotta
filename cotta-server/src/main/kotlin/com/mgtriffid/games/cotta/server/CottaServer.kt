package com.mgtriffid.games.cotta.server

import jakarta.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CottaServer @Inject constructor(
    private val instance: CottaGameInstance
) {
    fun initializeInstances() {
        // TODO make multi-threaded
        //  start a thread that will just run
        // TODO introduce a callback that is like "ready to connect"
        //  because if we connect too soon then there's not enough ticks for lagcomp on start
        val thread = Thread { instance.run() }
        thread.name = "game-main-loop-thread-0"
        thread.start()
    }
}
