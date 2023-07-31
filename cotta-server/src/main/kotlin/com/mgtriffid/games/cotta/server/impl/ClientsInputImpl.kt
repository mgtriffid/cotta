package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.ClientsInput

class ClientsInputImpl<IR: InputRecipe>(
    val network: CottaServerNetwork,
    val inputSerialization: InputSerialization<IR>,
    val inputSnapper: InputSnapper<IR>,
    val clientsGhosts: ClientsGhosts,
) : ClientsInput {

    override fun getInput(): Map<EntityId, Collection<InputComponent<*>>> {
        // so we drain into the storage which is a bunch of buffers
        // then we figure what do we take for each particular connection
        // for that we use ghosts (inject them)
        // then we unpack the input recipe
        return network.drainInputs().map { (cId, dto) ->
            val recipe = inputSerialization.deserializeInputRecipe(dto.payload)
            inputSnapper.unpackInputRecipe(recipe).entries
        }.flatten().associate { it.key to it.value }
    }
}
