package com.mgtriffid.games.cotta.client

import com.google.inject.Guice
import com.mgtriffid.games.cotta.client.guice.CottaClientModule
import com.mgtriffid.games.cotta.core.CottaGame

class CottaClientFactory {
    fun create(game: CottaGame, input: CottaClientInput) : CottaClient {
        val module = CottaClientModule(game, input)
        val injector = Guice.createInjector(module)
        return injector.getInstance(CottaClient::class.java)
    }
}