package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.impl.ClientsInput
import com.mgtriffid.games.cotta.server.impl.ClientsGhosts
import com.mgtriffid.games.cotta.server.impl.ClientsInputProviderImpl

interface ClientsInputProvider {
    companion object {
        fun <IR : InputRecipe> create(
            network: CottaServerNetwork,
            inputSerialization: InputSerialization<IR>,
            inputSnapper: InputSnapper<IR>,
            clientsGhosts: ClientsGhosts,
        ): ClientsInputProvider = ClientsInputProviderImpl(
            network = network,
            inputSerialization = inputSerialization,
            inputSnapper = inputSnapper,
            clientsGhosts = clientsGhosts
        )
    }

    fun getInput(): ClientsInput
}
