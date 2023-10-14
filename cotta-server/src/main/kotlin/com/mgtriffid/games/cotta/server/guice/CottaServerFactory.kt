package com.mgtriffid.games.cotta.server.guice

import com.google.inject.Guice
import com.google.inject.Module
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.server.CottaServer

class CottaServerFactory {
    fun create(game: CottaGame): CottaServer {
        val module: Module = CottaServerModule(game)
        val injector = Guice.createInjector(module)
        return injector.getInstance(CottaServer::class.java)
    }
}
