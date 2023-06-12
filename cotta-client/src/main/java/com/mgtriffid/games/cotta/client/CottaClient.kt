package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.CottaClientImpl
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.network.CottaClientNetwork

interface CottaClient {
    companion object {
        fun <SR: StateRecipe, DR: DeltaRecipe> getInstance(
            game: CottaGame,
            engine: CottaEngine<SR, DR>,
            network: CottaClientNetwork,
            input: CottaClientInput
        ): CottaClient = CottaClientImpl(game, engine, network, input, 8, 3)
    }

    fun initialize()

    fun tick()
}
