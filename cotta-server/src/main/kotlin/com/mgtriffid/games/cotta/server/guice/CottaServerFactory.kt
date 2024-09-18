package com.mgtriffid.games.cotta.server.guice

import com.google.inject.Guice
import com.google.inject.Module
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.server.CottaServer
import java.io.FileReader
import java.util.Properties

class CottaServerFactory {
    fun create(game: CottaGame): CottaServer {
        val properties = Properties()
        properties.load(FileReader("assets/config/cotta.properties"))
        val module: Module = CottaServerModule(game, java.lang.Boolean.valueOf(properties["cotta.core.debug.arrays"] as String) as Boolean)
        val injector = Guice.createInjector(module)
        return injector.getInstance(CottaServer::class.java)
    }
}
