package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.core.logging.configureLogging
import com.mgtriffid.games.panna.lobby.PannaLobby

object PannaServerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        configureLogging()
        PannaLobby().start()
        // lobby can receive requests to join a room or start a room, so perhaps
        // we first instantiate a Server that runs somehow, then we make it possible
        // to call some methods of Server from Lobby. Like join, stop, create new, etc.
        Thread {
            println("Starting server")
            PannaServer().start()
        }.start()
    }
}
