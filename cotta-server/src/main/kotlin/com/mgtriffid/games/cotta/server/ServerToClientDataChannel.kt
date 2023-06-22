package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.impl.ClientsGhosts
import com.mgtriffid.games.cotta.server.impl.ServerToClientDataChannelImpl

interface ServerToClientDataChannel {
    fun send(data: DataForClients)

    companion object {
        fun <SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe>getInstance(
            tickProvider: TickProvider,
            clientsGhosts: ClientsGhosts,
            network: CottaServerNetwork,
            stateSnapper: StateSnapper<SR, DR>,
            snapsSerialization: SnapsSerialization<SR, DR>,
            inputSnapper: InputSnapper<IR>,
            inputSerialization: InputSerialization<IR>,
        ): ServerToClientDataChannel = ServerToClientDataChannelImpl(
            tick = tickProvider,
            clientsGhosts = clientsGhosts,
            network = network,
            stateSnapper = stateSnapper,
            snapsSerialization = snapsSerialization,
            inputSnapper = inputSnapper,
            inputSerialization = inputSerialization
        )
    }
}
