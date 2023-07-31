package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.impl.ClientsGhosts
import com.mgtriffid.games.cotta.server.impl.ClientsInputImpl

interface ClientsInput {
    companion object {
        fun <IR : InputRecipe> create(
            network: CottaServerNetwork,
            inputSerialization: InputSerialization<IR>,
            inputSnapper: InputSnapper<IR>,
            clientsGhosts: ClientsGhosts,
        ): ClientsInput = ClientsInputImpl(
            network = network,
            inputSerialization = inputSerialization,
            inputSnapper = inputSnapper,
            clientsGhosts = clientsGhosts
        )
    }

    fun getInput(): Map<EntityId, Collection<InputComponent<*>>>
}
